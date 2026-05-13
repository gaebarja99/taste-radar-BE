package com.tasteradar.domain.user.api.dto;

public record UserProfileResponse(
		long id,
		String email,
		String nickname,
		String role,
		TastePreferencesResponse tastePreferences,
		String zipCode,
		String address,
		String addressDetail,
		boolean hasPassword
) {
}
