package com.tasteradar.oauth.kakao;

public interface KakaoOAuth2Service {

	KakaoUserProfile fetchUserProfile(String accessToken);
}
