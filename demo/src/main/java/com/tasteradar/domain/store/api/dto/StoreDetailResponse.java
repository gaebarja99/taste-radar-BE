package com.tasteradar.domain.store.api.dto;

import com.tasteradar.domain.store.entity.StoreStatus;
import java.util.List;

public record StoreDetailResponse(
		long id,
		String name,
		StoreStatus status,
		String openTime,
		String closeTime,
		int requiredTimeMinutes,
		int minOrderAmount,
		double averageRating,
		long reviewCount,
		List<StoreImageUrlResponse> images,
		List<StoreMenuResponse> menus
) {
}
