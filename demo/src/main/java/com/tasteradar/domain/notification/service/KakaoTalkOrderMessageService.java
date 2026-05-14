package com.tasteradar.domain.notification.service;

import tools.jackson.databind.json.JsonMapper;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderStatus;
import com.tasteradar.domain.user.entity.User;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 주문 상태 카카오톡 알림.
 * <ul>
 *   <li>로그인 시 저장된 사용자 OAuth 토큰으로 「나에게 보내기」 API 시도</li>
 *   <li>{@code kakao.talk.webhook-url} 이 있으면 알림톡 연동용 JSON POST</li>
 * </ul>
 */
@Slf4j
@Service
public class KakaoTalkOrderMessageService {

	private static final String KAKAO_MEMO_API = "https://kapi.kakao.com/v2/api/talk/memo/default/send";
	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final RestClient restClient = RestClient.create();

	@Value("${kakao.talk.enabled:false}")
	private boolean talkEnabled;

	@Value("${kakao.talk.webhook-url:}")
	private String webhookUrl;

	@Value("${app.oauth-success-redirect:http://localhost:5173/pages/auth/callback.html}")
	private String frontendRedirect;

	public void send(FoodOrder order, OrderStatus status, String message) {
		User user = order.getUser();
		Long kakaoId = resolveKakaoId(user);
		String orderUrl = orderDetailUrl(order.getId());

		log.info(
				"[KakaoTalk] orderId={} userId={} kakaoId={} status={} message={}",
				order.getId(),
				user.getId(),
				kakaoId,
				status,
				message
		);

		if (!talkEnabled) {
			return;
		}

		boolean sent = tryMemoSend(user, message, orderUrl);
		if (!sent) {
			postWebhook(user, kakaoId, order, status, message, orderUrl);
		}
	}

	private boolean tryMemoSend(User user, String message, String orderUrl) {
		String token = user.getKakaoTalkAccessToken();
		if (token == null || token.isBlank()) {
			return false;
		}

		Map<String, Object> template = new LinkedHashMap<>();
		template.put("object_type", "text");
		template.put("text", message);
		template.put("link", Map.of(
				"web_url", orderUrl,
				"mobile_web_url", orderUrl
		));

		try {
			restClient.post()
					.uri(KAKAO_MEMO_API)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + token)
					.body(JSON.writeValueAsString(Map.of("template_object", template)))
					.retrieve()
					.toBodilessEntity();
			return true;
		} catch (Exception e) {
			log.warn("KakaoTalk memo send failed for user {}: {}", user.getId(), e.getMessage());
			return false;
		}
	}

	private void postWebhook(
			User user,
			Long kakaoId,
			FoodOrder order,
			OrderStatus status,
			String message,
			String orderUrl
	) {
		if (webhookUrl.isBlank()) {
			return;
		}

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("userId", user.getId());
		payload.put("kakaoId", kakaoId);
		payload.put("orderId", order.getId());
		payload.put("orderStatus", status.name());
		payload.put("message", message);
		payload.put("linkUrl", orderUrl);

		try {
			restClient.post()
					.uri(webhookUrl)
					.contentType(MediaType.APPLICATION_JSON)
					.body(JSON.writeValueAsString(payload))
					.retrieve()
					.toBodilessEntity();
		} catch (Exception e) {
			log.warn("KakaoTalk webhook failed for order {}: {}", order.getId(), e.getMessage());
		}
	}

	private Long resolveKakaoId(User user) {
		if (user.getKakaoId() != null) {
			return user.getKakaoId();
		}
		String email = user.getEmail();
		if (email != null && email.startsWith("kakao_") && email.endsWith("@users.tasteradar.local")) {
			String idPart = email.substring("kakao_".length(), email.indexOf('@'));
			try {
				return Long.parseLong(idPart);
			} catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}

	private String orderDetailUrl(long orderId) {
		String base = frontendRedirect;
		int slash = base.indexOf("/pages/");
		if (slash > 0) {
			base = base.substring(0, slash);
		} else if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/pages/my-orders.html?orderId=" + orderId;
	}
}
