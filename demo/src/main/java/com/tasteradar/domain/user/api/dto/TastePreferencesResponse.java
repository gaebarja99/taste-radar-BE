package com.tasteradar.domain.user.api.dto;

public record TastePreferencesResponse(
		boolean sweet,
		boolean salty,
		boolean sour,
		boolean bitter,
		boolean umami
) {
}
