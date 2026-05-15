package com.tasteradar.security.service;

import com.tasteradar.domain.user.entity.User;
import com.tasteradar.domain.user.entity.UserRole;
import com.tasteradar.domain.user.repository.UserRepository;
import com.tasteradar.security.api.dto.AuthTokenResponse;
import com.tasteradar.security.api.dto.LoginRequest;
import com.tasteradar.security.api.dto.RegisterRequest;
import com.tasteradar.security.provider.JwtTokenProvider;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LocalAuthService {

	private static final Pattern PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");
	private static final Pattern NICKNAME = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	@Transactional
	public AuthTokenResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		String nickname = request.nickname().trim();
		String password = request.password();
		UserRole role = parseRole(request.role());

		if (!PASSWORD.matcher(password).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이며 영문과 숫자를 포함해야 해요.");
		}
		if (!NICKNAME.matcher(nickname).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "닉네임은 2~10자의 한글·영문·숫자만 사용할 수 있어요.");
		}

		User user = userRepository.findByEmailIncludingDeleted(email)
				.map(existing -> {
					if (!existing.isDeleted()) {
						throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일이에요.");
					}
					existing.setDeleted(false);
					existing.setNickname(nickname);
					existing.setRole(role);
					existing.setPasswordHash(passwordEncoder.encode(password));
					return existing;
				})
				.orElseGet(() -> {
					User created = new User();
					created.setEmail(email);
					created.setNickname(nickname);
					created.setRole(role);
					created.setPasswordHash(passwordEncoder.encode(password));
					created.setDeleted(false);
					return userRepository.save(created);
				});

		return issueTokens(user);
	}

	@Transactional(readOnly = true)
	public AuthTokenResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않아요."));

		String hash = user.getPasswordHash();
		if (hash == null || hash.isBlank()) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "카카오로 가입한 계정이에요. 카카오 로그인을 이용하거나 프로필에서 비밀번호를 설정해 주세요.");
		}
		if (!passwordEncoder.matches(request.password(), hash)) {
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않아요.");
		}

		return issueTokens(user);
	}

	private AuthTokenResponse issueTokens(User user) {
		String access = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
		String refresh = jwtTokenProvider.createRefreshToken(user.getId());
		refreshTokenService.save(user.getId(), refresh);
		return new AuthTokenResponse(
				access,
				refresh,
				user.getId(),
				user.getEmail(),
				user.getNickname(),
				user.getRole().name());
	}

	private static String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase();
	}

	private static UserRole parseRole(String role) {
		try {
			return UserRole.valueOf(role.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "역할은 CUSTOMER 또는 OWNER 여야 해요.");
		}
	}
}
