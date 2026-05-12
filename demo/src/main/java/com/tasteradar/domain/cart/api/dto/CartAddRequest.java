package com.tasteradar.domain.cart.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartAddRequest(
		@NotNull Long storeId,
		@NotNull Long menuId,
		@NotNull @Positive Integer quantity
) {
}
