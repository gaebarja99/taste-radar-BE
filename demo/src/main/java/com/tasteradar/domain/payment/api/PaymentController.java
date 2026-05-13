package com.tasteradar.domain.payment.api;

import com.tasteradar.domain.payment.api.dto.KakaoPayApproveRequest;
import com.tasteradar.domain.payment.api.dto.KakaoPayApproveResponse;
import com.tasteradar.domain.payment.api.dto.KakaoPayCancelRequest;
import com.tasteradar.domain.payment.api.dto.KakaoPayReadyRequest;
import com.tasteradar.domain.payment.api.dto.KakaoPayReadyResponse;
import com.tasteradar.domain.payment.service.KakaoPayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/kakaopay")
public class PaymentController {

	private final KakaoPayService kakaoPayService;

	@PostMapping("/ready")
	public KakaoPayReadyResponse ready(Authentication authentication, @Valid @RequestBody KakaoPayReadyRequest request) {
		return kakaoPayService.ready(parseUserId(authentication), request.orderId());
	}

	@PostMapping("/approve")
	public KakaoPayApproveResponse approve(Authentication authentication, @Valid @RequestBody KakaoPayApproveRequest request) {
		return kakaoPayService.approve(parseUserId(authentication), request.orderId(), request.pgToken());
	}

	@PostMapping("/cancel")
	public void cancel(Authentication authentication, @Valid @RequestBody KakaoPayCancelRequest request) {
		kakaoPayService.cancel(parseUserId(authentication), request.orderId(), request.reason());
	}

	private static long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
