package com.tasteradar.domain.ai.api.dto;

public record TastePentagonResponse(
		int sweetness,
		int saltiness,
		int sourness,
		int bitterness,
		int umami
) {
}

