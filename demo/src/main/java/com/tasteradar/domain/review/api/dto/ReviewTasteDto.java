package com.tasteradar.domain.review.api.dto;

public record ReviewTasteDto(
		int sweetness,
		int saltiness,
		int sourness,
		int bitterness,
		int umami
) {
}
