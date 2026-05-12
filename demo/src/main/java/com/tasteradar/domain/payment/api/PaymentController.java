package com.tasteradar.domain.payment.api;

import com.tasteradar.domain.payment.api.dto.KakaoPayApproveRequest;
import com.tasteradar.domain.payment.api.dto.KakaoPayCancelRequest;
import com.tasteradar.domain.payment.api.dto.KakaoPayReadyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * 카카오페이 연동은 별도 Admin Key·CID·REST 호출 설정이 필요합니다.
 * 엔드포인트 스펙만 두고 501로 응답합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/kakaopay")
public class PaymentController {

	@PostMapping("/ready")
	public void ready(@Valid @RequestBody KakaoPayReadyRequest request) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "KakaoPay integration not configured");
	}

	@PostMapping("/approve")
	public void approve(@Valid @RequestBody KakaoPayApproveRequest request) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "KakaoPay integration not configured");
	}

	@PostMapping("/cancel")
	public void cancel(@Valid @RequestBody KakaoPayCancelRequest request) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "KakaoPay integration not configured");
	}
}
