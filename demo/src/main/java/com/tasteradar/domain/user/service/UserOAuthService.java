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
	 * <ul>
	 *   <li>신규: {@code pendingRole} 쿠키(OWNER/CUSTOMER)로 역할 지정, 없으면 CUSTOMER.</li>
	 *   <li>기존: 닉네임 갱신 + 소프트 삭제면 복구 + 로그인 진입 시 선택한 {@code pendingRole} 이 있으면 그 역할로 갱신
	 *       (메인의 「고객/사장으로 시작」과 JWT 역할을 맞추기 위함).</li>
	 * </ul>
	 */
	@Transactional
	public User upsertKakaoUser(KakaoUserProfile profile, String pendingRole) {
		return upsertKakaoUser(profile, pendingRole, null);
	}

	@Transactional
	public User upsertKakaoUser(KakaoUserProfile profile, String pendingRole, String kakaoTalkAccessToken) {
		return userRepository.findByEmailIncludingDeleted(profile.email())
				.map(existing -> {
					existing.setNickname(profile.nickname());
					existing.setKakaoId(profile.kakaoId());
					if (kakaoTalkAccessToken != null && !kakaoTalkAccessToken.isBlank()) {
						existing.setKakaoTalkAccessToken(kakaoTalkAccessToken);
					}
					if (existing.isDeleted()) {
						existing.setDeleted(false);
					}
					if (pendingRole != null && !pendingRole.isBlank()) {
						existing.setRole(resolveRole(pendingRole));
					}
					return existing;
				})
				.orElseGet(() -> {
					User user = new User();
					user.setEmail(profile.email());
					user.setNickname(profile.nickname());
					user.setKakaoId(profile.kakaoId());
					if (kakaoTalkAccessToken != null && !kakaoTalkAccessToken.isBlank()) {
						user.setKakaoTalkAccessToken(kakaoTalkAccessToken);
					}
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
