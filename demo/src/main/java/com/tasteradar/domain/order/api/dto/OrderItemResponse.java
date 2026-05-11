package com.tasteradar.domain.order.api.dto;

public record OrderItemResponse(
		long menuId,
		String menuName,
		int quantity,
		int unitPrice,
		int lineAmount
) {
}
