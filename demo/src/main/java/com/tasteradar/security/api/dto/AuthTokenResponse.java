package com.tasteradar.security.api.dto;

/**
 * 로그인·회원가입 성공 시 JWT 및 사용자 요약.
 */
public record AuthTokenResponse(
		String accessToken,
		String refreshToken,
		long userId,
		String email,
		String nickname,
		String role
) {
}
