package com.tasteradar.domain.payment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KakaoPayApproveRequest(
		@NotNull Long orderId,
		@NotBlank String pgToken
) {
}
