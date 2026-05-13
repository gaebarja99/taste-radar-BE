package com.tasteradar.domain.store.api.dto;

public record StoreTasteHighlightResponse(
		String key,
		String label,
		int score
) {
}
