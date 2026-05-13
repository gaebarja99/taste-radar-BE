package com.tasteradar.domain.review.api.dto;

public record ReviewTasteDto(
		boolean sweet,
		boolean salty,
		boolean sour,
		boolean bitter,
		boolean umami
) {
}
