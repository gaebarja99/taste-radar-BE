package com.tasteradar.domain.payment.api.dto;

import jakarta.validation.constraints.NotNull;

public record KakaoPayReadyRequest(
		@NotNull Long orderId
) {
}
