package com.tasteradar.domain.cart.api.dto;

public record CartItemResponse(
		long id,
		long menuId,
		String menuName,
		long unitPrice,
		int quantity
) {
}
