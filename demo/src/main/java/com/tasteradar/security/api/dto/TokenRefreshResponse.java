package com.tasteradar.security.api.dto;

public record TokenRefreshResponse(
		String accessToken,
		String refreshToken
) {
}

