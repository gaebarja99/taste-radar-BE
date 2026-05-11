package com.tasteradar.domain.cart.api.dto;

import java.util.List;

public record CartResponse(
		Long storeId,
		String storeName,
		List<CartItemResponse> items
) {
}
