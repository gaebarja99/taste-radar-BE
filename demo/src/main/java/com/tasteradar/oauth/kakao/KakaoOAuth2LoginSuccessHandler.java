package com.tasteradar.oauth.kakao;

import com.tasteradar.domain.user.entity.User;
import com.tasteradar.domain.user.service.UserOAuthService;
import com.tasteradar.security.provider.JwtTokenProvider;
import com.tasteradar.security.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 브라우저에서 {@code /oauth2/authorization/kakao} 로그인 후 콜백이 성공하면 호출됩니다.
 * 카카오 프로필로 사용자를 저장·갱신한 뒤, 발급한 JWT를 프론트 콜백 페이지의
 * URL fragment({@code #})에 담아 redirect 합니다. fragment는 서버로 전송되지 않아 토큰 노출이 적습니다.
 * <p>
 * 신규 가입자라면 {@link KakaoLoginStartController}에서 저장한 {@code pending_role} 쿠키 값을
 * 그대로 적용합니다. 기존 사용자라면 역할은 변경되지 않습니다.
 */
@Component
@RequiredArgsConstructor
public class KakaoOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final UserOAuthService userOAuthService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	@Value("${app.oauth-success-redirect}")
	private String successRedirect;

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

		String pendingRole = readPendingRole(request);
		clearPendingRoleCookie(response);

		User user = userOAuthService.upsertKakaoUser(profile, pendingRole);

		String access = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
		String refresh = jwtTokenProvider.createRefreshToken(user.getId());
		refreshTokenService.save(user.getId(), refresh);

		String url = successRedirect + "#"
				+ "accessToken=" + encode(access)
				+ "&refreshToken=" + encode(refresh)
				+ "&userId=" + user.getId()
				+ "&email=" + encode(nullToEmpty(user.getEmail()))
				+ "&nickname=" + encode(nullToEmpty(user.getNickname()))
				+ "&role=" + encode(user.getRole().name());
		response.sendRedirect(url);
	}

	private static String readPendingRole(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) return null;
		for (Cookie c : cookies) {
			if (KakaoLoginStartController.PENDING_ROLE_COOKIE.equals(c.getName())) {
				return c.getValue();
			}
		}
		return null;
	}

	private static void clearPendingRoleCookie(HttpServletResponse response) {
		response.addHeader("Set-Cookie",
				KakaoLoginStartController.PENDING_ROLE_COOKIE + "=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax");
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
