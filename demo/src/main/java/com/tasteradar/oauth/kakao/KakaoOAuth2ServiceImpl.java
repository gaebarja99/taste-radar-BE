package com.tasteradar.oauth.kakao;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * 카카오에서 발급한 액세스 토큰으로 사용자 정보를 조회합니다.
 * (모바일 SDK 등에서 직접 토큰을 넘기는 REST API용. 브라우저 OAuth2 로그인은 Spring이
 * {@link org.springframework.security.oauth2.core.user.OAuth2User}로 처리합니다.)
 */
@Service
@RequiredArgsConstructor
public class KakaoOAuth2ServiceImpl implements KakaoOAuth2Service {

	private final JsonMapper jsonMapper;

	@Override
	public KakaoUserProfile fetchUserProfile(String accessToken) {
		String json = RestClient.create()
				.get()
				.uri("https://kapi.kakao.com/v2/user/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.retrieve()
				.body(String.class);
		if (json == null || json.isBlank()) {
			throw new IllegalStateException("Empty Kakao user response");
		}
		try {
			return KakaoUserAttributeMapper.fromJsonNode(jsonMapper.readTree(json));
		} catch (JacksonException e) {
			throw new IllegalStateException("Invalid Kakao user JSON", e);
		}
	}
}
