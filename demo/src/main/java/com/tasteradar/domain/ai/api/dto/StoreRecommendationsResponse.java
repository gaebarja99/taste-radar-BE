package com.tasteradar.domain.ai.api.dto;

import java.util.List;

public record StoreRecommendationsResponse(
		String message,
		List<RecommendedMenu> menus,
		String source
) {
	public StoreRecommendationsResponse(String message, List<RecommendedMenu> menus) {
		this(message, menus, "RULE");
	}

	public record RecommendedMenu(long menuId, String name, long price, String imageUrl) {
	}
}
