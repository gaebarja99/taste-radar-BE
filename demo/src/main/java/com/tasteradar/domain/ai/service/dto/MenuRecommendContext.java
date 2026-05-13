package com.tasteradar.domain.ai.service.dto;

import com.tasteradar.domain.store.api.dto.StoreTasteProfileResponse;
import com.tasteradar.domain.user.entity.UserTastePreference;
import java.util.List;

public record MenuRecommendContext(
		String storeName,
		UserTastePreference userTaste,
		StoreTasteProfileResponse storeProfile,
		List<MenuCandidate> menus
) {
	public record MenuCandidate(long id, String name, String description, long price) {
	}
}
