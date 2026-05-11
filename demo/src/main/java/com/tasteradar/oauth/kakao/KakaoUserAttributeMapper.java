package com.tasteradar.oauth.kakao;

import java.util.Map;
import org.springframework.security.oauth2.core.user.OAuth2User;
import tools.jackson.databind.JsonNode;

public final class KakaoUserAttributeMapper {

	private KakaoUserAttributeMapper() {
	}

	public static KakaoUserProfile fromOAuth2User(OAuth2User user) {
		return fromAttributeMap(user.getAttributes());
	}

	/** {@link KakaoOAuth2ServiceImpl} 등에서 카카오 API 원문 JSON을 트리로 파싱한 뒤 사용 */
	public static KakaoUserProfile fromJsonNode(JsonNode root) {
		long kakaoId = root.path("id").asLong();
		if (kakaoId == 0) {
			throw new IllegalArgumentException("Kakao user id missing in response");
		}
		JsonNode account = root.path("kakao_account");
		String email = textOrNull(account, "email");
		if (email == null || email.isBlank()) {
			email = "kakao_" + kakaoId + "@users.tasteradar.local";
		}
		String nickname = textOrNull(account.path("profile"), "nickname");
		if (nickname == null || nickname.isBlank()) {
			nickname = "카카오";
		}
		nickname = truncateNickname(nickname);
		return new KakaoUserProfile(kakaoId, nickname, email);
	}

	private static String textOrNull(JsonNode parent, String field) {
		JsonNode n = parent.path(field);
		return n.isMissingNode() || n.isNull() || !n.isString() ? null : n.asString();
	}

	public static KakaoUserProfile fromAttributeMap(Map<String, Object> attributes) {
		Object idObj = attributes.get("id");
		long kakaoId = idObj instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(idObj));
		Object kaObj = attributes.get("kakao_account");
		String email = null;
		String nickname = null;
		if (kaObj instanceof Map<?, ?> ka) {
			Object e = ka.get("email");
			if (e != null) {
				email = String.valueOf(e);
			}
			Object p = ka.get("profile");
			if (p instanceof Map<?, ?> profile) {
				Object n = profile.get("nickname");
				if (n != null) {
					nickname = String.valueOf(n);
				}
			}
		}
		if (email == null || email.isBlank()) {
			email = "kakao_" + kakaoId + "@users.tasteradar.local";
		}
		if (nickname == null || nickname.isBlank()) {
			nickname = "카카오";
		}
		nickname = truncateNickname(nickname);
		return new KakaoUserProfile(kakaoId, nickname, email);
	}

	/** DB 컬럼 nickname varchar(10) 제한 */
	private static String truncateNickname(String nickname) {
		return nickname.length() <= 10 ? nickname : nickname.substring(0, 10);
	}
}
