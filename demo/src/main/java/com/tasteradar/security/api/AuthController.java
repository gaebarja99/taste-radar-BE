package com.tasteradar.security.api;

import com.tasteradar.domain.user.repository.UserRepository;
import com.tasteradar.security.api.dto.AuthTokenResponse;
import com.tasteradar.security.api.dto.LoginRequest;
import com.tasteradar.security.api.dto.RegisterRequest;
import com.tasteradar.security.api.dto.TokenRefreshRequest;
import com.tasteradar.security.api.dto.TokenRefreshResponse;
import com.tasteradar.security.provider.JwtTokenProvider;
import com.tasteradar.security.service.LocalAuthService;
import com.tasteradar.security.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;
	private final LocalAuthService localAuthService;

	@PostMapping("/register")
	public AuthTokenResponse register(@Valid @RequestBody RegisterRequest request) {
		return localAuthService.register(request);
	}

	@PostMapping("/login")
	public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
		return localAuthService.login(request);
	}

	@PostMapping("/refresh")
	public TokenRefreshResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
		String refreshToken = request.refreshToken();
		if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
		}

		long userId = jwtTokenProvider.getUserId(refreshToken);
		if (!refreshTokenService.matches(userId, refreshToken)) {
			// possible theft / already rotated
			refreshTokenService.delete(userId);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token mismatch");
		}

		var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
		String newAccess = jwtTokenProvider.createAccessToken(userId, user.getRole().name());
		String newRefresh = jwtTokenProvider.createRefreshToken(userId);
		refreshTokenService.rotate(userId, newRefresh);

		return new TokenRefreshResponse(newAccess, newRefresh);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(Authentication authentication) {
		long userId = parseUserId(authentication);
		refreshTokenService.delete(userId);
	}

	private long parseUserId(Authentication authentication) {
		if (authentication == null || authentication.getPrincipal() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}

