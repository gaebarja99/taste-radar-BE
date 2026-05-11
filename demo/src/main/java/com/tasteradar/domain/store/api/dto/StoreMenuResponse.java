package com.tasteradar.domain.store.api.dto;

public record StoreMenuResponse(
		long id,
		String name,
		long price,
		String menuDescription,
		String imageUrl
) {
}
