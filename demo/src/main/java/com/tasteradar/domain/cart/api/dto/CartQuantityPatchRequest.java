package com.tasteradar.domain.cart.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartQuantityPatchRequest(
		@NotNull @Positive Integer quantity
) {
}
