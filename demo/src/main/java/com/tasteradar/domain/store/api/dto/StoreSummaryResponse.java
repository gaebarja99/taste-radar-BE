package com.tasteradar.domain.store.api.dto;

import com.tasteradar.domain.store.entity.StoreStatus;
import java.util.List;

public record StoreSummaryResponse(
		long id,
		String name,
		StoreStatus status,
		int minOrderAmount,
		double averageRating,
		long reviewCount,
		String thumbnailUrl,
		Double latitude,
		Double longitude,
		List<StoreTasteHighlightResponse> tasteHighlights
) {
}
