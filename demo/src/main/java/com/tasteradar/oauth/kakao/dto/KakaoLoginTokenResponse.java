package com.tasteradar.oauth.kakao.dto;

public record KakaoLoginTokenResponse(
		String accessToken,
		String refreshToken,
		long userId,
		String email,
		String nickname
) {
}
