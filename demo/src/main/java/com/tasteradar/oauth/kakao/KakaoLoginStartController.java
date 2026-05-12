package com.tasteradar.oauth.kakao;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카카오 로그인 진입 헬퍼.
 * <p>
 * 프론트에서 사용자가 역할(OWNER/CUSTOMER)을 먼저 선택한 뒤 이 엔드포인트로 진입하면,
 * 선택된 역할을 {@code pending_role} 쿠키에 잠시 저장한 후 표준 OAuth2 진입점
 * ({@code /oauth2/authorization/kakao})으로 리다이렉트합니다.
 * 이후 로그인 성공 핸들러가 이 쿠키를 읽어 신규 가입자에 한해 역할을 적용합니다.
 */
@RestController
@RequestMapping("/api/auth/kakao")
public class KakaoLoginStartController {

	public static final String PENDING_ROLE_COOKIE = "pending_role";
	private static final int COOKIE_MAX_AGE_SECONDS = 300;

	@GetMapping("/start")
	public void start(
			@RequestParam(name = "role", required = false) String role,
			HttpServletResponse response
	) throws IOException {
		String normalized = normalize(role);
		if (normalized != null) {
			response.addHeader("Set-Cookie",
					PENDING_ROLE_COOKIE + "=" + normalized
							+ "; Max-Age=" + COOKIE_MAX_AGE_SECONDS
							+ "; Path=/; HttpOnly; SameSite=Lax");
		}
		response.sendRedirect("/oauth2/authorization/kakao");
	}

	private static String normalize(String role) {
		if (role == null) return null;
		String upper = role.trim().toUpperCase();
		if ("OWNER".equals(upper) || "CUSTOMER".equals(upper)) {
			return upper;
		}
		return null;
	}
}
