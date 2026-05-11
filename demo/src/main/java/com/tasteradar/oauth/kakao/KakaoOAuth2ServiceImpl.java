package com.tasteradar.oauth.kakao;

import org.springframework.stereotype.Service;

@Service
public class KakaoOAuth2ServiceImpl implements KakaoOAuth2Service {

	@Override
	public KakaoUserProfile fetchUserProfile(String accessToken) {
		throw new UnsupportedOperationException(
				"TODO: GET https://kapi.kakao.com/v2/user/me with Bearer token and map kakao_account/profile fields.");
	}
}
