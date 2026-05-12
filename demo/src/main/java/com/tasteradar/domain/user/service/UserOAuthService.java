package com.tasteradar.domain.user.service;

import com.tasteradar.domain.user.entity.User;
import com.tasteradar.domain.user.entity.UserRole;
import com.tasteradar.domain.user.repository.UserRepository;
import com.tasteradar.oauth.kakao.KakaoUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserOAuthService {

	private final UserRepository userRepository;

	@Transactional
	public User upsertKakaoUser(KakaoUserProfile profile) {
		return upsertKakaoUser(profile, null);
	}

	/**
	 * 카카오 프로필로 사용자 upsert.
	 * 신규 가입자라면 {@code pendingRole}을 OWNER/CUSTOMER로 해석해 적용합니다.
	 * 기존 사용자라면 역할은 변경하지 않습니다(위변조 방지).
	 */
	@Transactional
	public User upsertKakaoUser(KakaoUserProfile profile, String pendingRole) {
		return userRepository.findByEmail(profile.email())
				.map(existing -> {
					existing.setNickname(profile.nickname());
					return existing;
				})
				.orElseGet(() -> {
					User user = new User();
					user.setEmail(profile.email());
					user.setNickname(profile.nickname());
					user.setRole(resolveRole(pendingRole));
					user.setDeleted(false);
					return userRepository.save(user);
				});
	}

	private UserRole resolveRole(String pending) {
		if (pending == null || pending.isBlank()) {
			return UserRole.CUSTOMER;
		}
		try {
			return UserRole.valueOf(pending.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return UserRole.CUSTOMER;
		}
	}
}
