package com.tasteradar.domain.user.api.dto;

public record TasteUpdateRequest(
		boolean sweet,
		boolean salty,
		boolean sour,
		boolean bitter,
		boolean umami
) {
}
