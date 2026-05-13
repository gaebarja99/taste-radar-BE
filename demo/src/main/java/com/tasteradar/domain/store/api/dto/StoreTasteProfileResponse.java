package com.tasteradar.domain.store.api.dto;

public record StoreTasteProfileResponse(
		int sweetness,
		int saltiness,
		int sourness,
		int bitterness,
		int umami,
		long reviewCount
) {
}
