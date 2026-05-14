package com.tasteradar.domain.review.api.dto;

public record ReviewMenuTasteResponse(
		long menuId,
		String menuName,
		String taste
) {
}
