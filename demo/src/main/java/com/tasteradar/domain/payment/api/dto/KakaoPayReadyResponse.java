package com.tasteradar.domain.payment.api.dto;

public record KakaoPayReadyResponse(
		Long orderId,
		String tid,
		String nextRedirectPcUrl,
		String nextRedirectMobileUrl
) {
}
