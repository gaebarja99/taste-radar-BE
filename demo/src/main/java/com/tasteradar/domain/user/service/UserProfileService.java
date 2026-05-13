package com.tasteradar.domain.user.service;

import com.tasteradar.domain.user.api.dto.TastePreferencesResponse;
import com.tasteradar.domain.user.api.dto.UserProfileResponse;
import com.tasteradar.domain.user.entity.User;
import com.tasteradar.domain.user.entity.UserRole;
import com.tasteradar.domain.user.entity.UserTastePreference;
import com.tasteradar.domain.user.repository.UserRepository;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private static final Pattern NICKNAME = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");
	private static final Pattern PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public UserProfileResponse getMe(long userId) {
		User user = loadUser(userId);
		return toResponse(user);
	}

	@Transactional
	public UserProfileResponse updateNickname(long userId, String nickname) {
		if (nickname == null || !NICKNAME.matcher(nickname).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "닉네임은 2~10자의 한글·영문·숫자만 사용할 수 있어요.");
		}
		User user = loadUser(userId);
		user.setNickname(nickname);
		return toResponse(user);
	}

	@Transactional
	public UserProfileResponse updateAddress(long userId, String zipCode, String address, String addressDetail) {
		if (zipCode == null || zipCode.isBlank() || address == null || address.isBlank()
				|| addressDetail == null || addressDetail.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배달 주소를 모두 입력해 주세요.");
		}
		User user = loadUser(userId);
		user.setZipCode(zipCode.trim());
		user.setAddress(address.trim());
		user.setAddressDetail(addressDetail.trim());
		return toResponse(user);
	}

	@Transactional
	public UserProfileResponse updatePassword(long userId, String currentPassword, String newPassword) {
		if (newPassword == null || !PASSWORD.matcher(newPassword).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이며 영문과 숫자를 포함해야 해요.");
		}
		User user = loadUser(userId);
		if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
			if (currentPassword == null || currentPassword.isBlank()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호를 입력해 주세요.");
			}
			if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않아요.");
			}
		}
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		return toResponse(user);
	}

	@Transactional
	public UserProfileResponse updateTastes(long userId, boolean sweet, boolean salty, boolean sour, boolean bitter, boolean umami) {
		if (!sweet && !salty && !sour && !bitter && !umami) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one taste must be true");
		}
		User user = loadUser(userId);
		if (user.getRole() != UserRole.CUSTOMER) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can set tastes");
		}
		UserTastePreference pref = user.getTastePreference();
		if (pref == null) {
			pref = new UserTastePreference();
			pref.setUser(user);
			user.setTastePreference(pref);
		}
		pref.setSweet(sweet);
		pref.setSalty(salty);
		pref.setSour(sour);
		pref.setBitter(bitter);
		pref.setUmami(umami);
		return toResponse(user);
	}

	@Transactional
	public void withdraw(long userId) {
		User user = loadUser(userId);
		userRepository.delete(user);
	}

	public User loadUser(long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	private UserProfileResponse toResponse(User user) {
		UserTastePreference t = user.getTastePreference();
		TastePreferencesResponse tastes = t == null
				? new TastePreferencesResponse(false, false, false, false, false)
				: new TastePreferencesResponse(t.isSweet(), t.isSalty(), t.isSour(), t.isBitter(), t.isUmami());
		return new UserProfileResponse(
				user.getId(),
				user.getEmail(),
				user.getNickname(),
				user.getRole().name(),
				tastes,
				user.getZipCode(),
				user.getAddress(),
				user.getAddressDetail(),
				user.getPasswordHash() != null && !user.getPasswordHash().isBlank()
		);
	}
}
