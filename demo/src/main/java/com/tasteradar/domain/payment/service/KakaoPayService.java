package com.tasteradar.domain.payment.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.tasteradar.domain.notification.service.OrderNotificationService;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderStatus;
import com.tasteradar.domain.order.repository.FoodOrderRepository;
import com.tasteradar.domain.payment.api.dto.KakaoPayApproveResponse;
import com.tasteradar.domain.payment.api.dto.KakaoPayReadyResponse;
import com.tasteradar.domain.payment.entity.Payment;
import com.tasteradar.domain.payment.repository.PaymentRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class KakaoPayService {

	private static final String KAKAO_PAY_API = "https://open-api.kakaopay.com/online/v1/payment";
	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final FoodOrderRepository foodOrderRepository;
	private final PaymentRepository paymentRepository;
	private final OrderNotificationService orderNotificationService;
	private final RestClient restClient = RestClient.create();

	@Value("${kakao.pay.cid:TC0ONETIME}")
	private String cid;

	@Value("${kakao.pay.admin-key:}")
	private String adminKey;

	@Value("${kakao.pay.approval-url:http://localhost:5173/pages/payment/kakao-success.html}")
	private String approvalUrlBase;

	@Value("${kakao.pay.cancel-url:http://localhost:5173/pages/checkout.html?canceled=1}")
	private String cancelUrl;

	@Value("${kakao.pay.fail-url:http://localhost:5173/pages/checkout.html?failed=1}")
	private String failUrl;

	@Transactional
	public KakaoPayReadyResponse ready(long userId, long orderId) {
		ensureAdminKey();
		FoodOrder order = loadUserOrder(userId, orderId);
		if (order.getOrderStatus() != OrderStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING orders can be paid");
		}

		paymentRepository.findByOrder_Id(orderId).ifPresent(existing -> {
			if ("APPROVED".equals(existing.getStatus())) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already paid");
			}
		});

		String itemName = order.getStore().getName();
		if (itemName.length() > 100) {
			itemName = itemName.substring(0, 100);
		}

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("cid", cid);
		body.put("partner_order_id", String.valueOf(order.getId()));
		body.put("partner_user_id", String.valueOf(userId));
		body.put("item_name", itemName);
		body.put("quantity", 1);
		body.put("total_amount", order.getTotalAmount());
		body.put("tax_free_amount", 0);
		body.put("approval_url", approvalUrlBase + "?orderId=" + order.getId());
		body.put("cancel_url", cancelUrl);
		body.put("fail_url", failUrl);

		JsonNode res = post("/ready", body);
		String tid = requiredText(res, "tid");

		Payment payment = paymentRepository.findByOrder_Id(orderId).orElseGet(Payment::new);
		payment.setOrder(order);
		payment.setTid(tid);
		payment.setCid(cid);
		payment.setPaymentMethod("KAKAOPAY");
		payment.setTotalPrice(order.getTotalAmount());
		payment.setStatus("READY");
		payment.setApprovedAt(null);
		payment.setCanceledAt(null);
		paymentRepository.save(payment);

		return new KakaoPayReadyResponse(
				order.getId(),
				tid,
				requiredText(res, "next_redirect_pc_url"),
				textOrNull(res, "next_redirect_mobile_url")
		);
	}

	@Transactional
	public KakaoPayApproveResponse approve(long userId, long orderId, String pgToken) {
		ensureAdminKey();
		FoodOrder order = loadUserOrder(userId, orderId);
		Payment payment = paymentRepository.findByOrder_IdAndOrder_User_Id(orderId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not ready"));

		if ("APPROVED".equals(payment.getStatus())) {
			return new KakaoPayApproveResponse(
					order.getId(),
					payment.getStatus(),
					payment.getTotalPrice(),
					payment.getApprovedAt()
			);
		}

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("cid", cid);
		body.put("tid", payment.getTid());
		body.put("partner_order_id", String.valueOf(order.getId()));
		body.put("partner_user_id", String.valueOf(userId));
		body.put("pg_token", pgToken);

		post("/approve", body);

		Instant approvedAt = Instant.now();
		payment.setStatus("APPROVED");
		payment.setApprovedAt(approvedAt);
		paymentRepository.save(payment);

		orderNotificationService.notifyOrderStatus(order, OrderStatus.PENDING);

		return new KakaoPayApproveResponse(
				order.getId(),
				payment.getStatus(),
				payment.getTotalPrice(),
				approvedAt
		);
	}

	@Transactional
	public void cancel(long userId, long orderId, String reason) {
		Payment payment = paymentRepository.findByOrder_IdAndOrder_User_Id(orderId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
		if (!"APPROVED".equals(payment.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only approved payments can be canceled");
		}
		cancelApprovedPayment(payment, reason);
	}

	@Transactional
	public void cancelApprovedForOrder(FoodOrder order, String reason) {
		paymentRepository.findByOrder_Id(order.getId()).ifPresent(payment -> {
			if ("APPROVED".equals(payment.getStatus())) {
				cancelApprovedPayment(payment, reason);
			}
		});
	}

	private void cancelApprovedPayment(Payment payment, String reason) {
		ensureAdminKey();
		if ("CANCELED".equals(payment.getStatus())) {
			return;
		}
		if (!"APPROVED".equals(payment.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only approved payments can be canceled");
		}

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("cid", cid);
		body.put("tid", payment.getTid());
		body.put("cancel_amount", payment.getTotalPrice());
		body.put("cancel_tax_free_amount", 0);
		if (reason != null && !reason.isBlank()) {
			String payload = reason.trim();
			if (payload.length() > 100) {
				payload = payload.substring(0, 100);
			}
			body.put("payload", payload);
		}

		try {
			post("/cancel", body);
		} catch (ResponseStatusException e) {
			if (e.getStatusCode() != HttpStatus.BAD_GATEWAY || !isAlreadyCanceledKakaoError(e.getReason())) {
				throw e;
			}
		}

		payment.setStatus("CANCELED");
		payment.setCanceledAt(Instant.now());
		paymentRepository.save(payment);
	}

	private static boolean isAlreadyCanceledKakaoError(String message) {
		if (message == null || message.isBlank()) {
			return false;
		}
		String m = message.toLowerCase();
		return m.contains("이미 취소")
				|| m.contains("already cancel")
				|| m.contains("already canceled")
				|| m.contains("already cancelled")
				|| m.contains("취소된 결제")
				|| m.contains("취소 가능한 금액")
				|| m.contains("(-781)")
				|| m.contains("(-782)")
				|| m.contains("error_code -781")
				|| m.contains("error_code -782");
	}

	private FoodOrder loadUserOrder(long userId, long orderId) {
		return foodOrderRepository.findByIdAndUser_Id(orderId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
	}

	private JsonNode post(String path, Map<String, Object> body) {
		try {
			return restClient.post()
					.uri(KAKAO_PAY_API + path)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "SECRET_KEY " + kakaoAdminKey())
					.body(body)
					.retrieve()
					.body(JsonNode.class);
		} catch (RestClientResponseException e) {
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					parseKakaoPayErrorMessage(e)
			);
		}
	}

	private static String parseKakaoPayErrorMessage(RestClientResponseException e) {
		String raw = e.getResponseBodyAsString();
		if (raw == null || raw.isBlank()) {
			return "KakaoPay API error: HTTP " + e.getStatusCode().value();
		}
		try {
			JsonNode node = JSON.readTree(raw);
			String code = textOrNull(node, "error_code");
			String message = textOrNull(node, "error_message");
			if (message != null && !message.isBlank()) {
				return code != null
						? "KakaoPay API error (" + code + "): " + message
						: "KakaoPay API error: " + message;
			}
		} catch (Exception ignored) {
			/* fall through */
		}
		return "KakaoPay API error: " + raw;
	}

	private void ensureAdminKey() {
		String key = kakaoAdminKey();
		if (key.isBlank()) {
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"KakaoPay admin key is not configured. Set KAKAO_PAY_ADMIN_KEY in taste-radar-BE/demo/.env and restart the server."
			);
		}
		if (key.length() != 40) {
			String hint = key.length() == 32
					? " (looks like Kakao REST API key — get Secret Key(dev) from https://developers.kakaopay.com)"
					: " (current length: " + key.length() + ")";
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"KakaoPay admin key must be a 40-character Secret Key from developers.kakaopay.com" + hint
			);
		}
	}

	private String kakaoAdminKey() {
		return adminKey.trim();
	}

	private static String requiredText(JsonNode node, String field) {
		JsonNode v = node.path(field);
		if (v.isMissingNode() || v.isNull() || v.asText().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KakaoPay response missing " + field);
		}
		return v.asText();
	}

	private static String textOrNull(JsonNode node, String field) {
		JsonNode v = node.path(field);
		return v.isMissingNode() || v.isNull() ? null : v.asText();
	}
}
