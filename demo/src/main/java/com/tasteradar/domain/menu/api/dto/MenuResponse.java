package com.tasteradar.domain.menu.api.dto;

public record MenuResponse(
		long id,
		String name,
		long price,
		String menuDescription,
		String imageUrl
) {
}
