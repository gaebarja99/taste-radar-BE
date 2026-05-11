package com.tasteradar.oauth.kakao;

import tools.jackson.databind.json.JsonMapper;
import com.tasteradar.domain.user.entity.User;
import com.tasteradar.domain.user.service.UserOAuthService;
import com.tasteradar.oauth.kakao.dto.KakaoLoginTokenResponse;
import com.tasteradar.security.provider.JwtTokenProvider;
import com.tasteradar.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 브라우저에서 {@code /oauth2/authorization/kakao} 로그인 후 콜백이 성공하면 호출됩니다.
 * 카카오에서 받은 프로필로 사용자를 저장·갱신하고 JWT를 JSON으로 반환합니다.
 */
@Component
@RequiredArgsConstructor
public class KakaoOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final UserOAuthService userOAuthService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final JsonMapper jsonMapper;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {
		if (!(authentication instanceof OAuth2AuthenticationToken token)) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected authentication type");
			return;
		}
		OAuth2User oauth2User = token.getPrincipal();
		KakaoUserProfile profile = KakaoUserAttributeMapper.fromOAuth2User(oauth2User);
		User user = userOAuthService.upsertKakaoUser(profile);

		String access = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
		String refresh = jwtTokenProvider.createRefreshToken(user.getId());
		refreshTokenService.save(user.getId(), refresh);

		var body = new KakaoLoginTokenResponse(
				access,
				refresh,
				user.getId(),
				user.getEmail(),
				user.getNickname()
		);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		jsonMapper.writeValue(response.getWriter(), body);
	}
}
