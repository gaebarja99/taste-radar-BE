package com.tasteradar.domain.order.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
		@NotNull Long storeId,
		@NotBlank String zipCode,
		@NotBlank String address,
		@NotBlank String addressDetail
) {
}
