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
		return userRepository.findByEmail(profile.email())
				.map(existing -> {
					existing.setNickname(profile.nickname());
					return existing;
				})
				.orElseGet(() -> {
					User user = new User();
					user.setEmail(profile.email());
					user.setNickname(profile.nickname());
					user.setRole(UserRole.CUSTOMER);
					user.setDeleted(false);
					return userRepository.save(user);
				});
	}
}
