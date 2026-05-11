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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private static final Pattern NICKNAME = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public UserProfileResponse getMe(long userId) {
		User user = loadUser(userId);
		return toResponse(user);
	}

	@Transactional
	public UserProfileResponse updateNickname(long userId, String nickname) {
		if (nickname == null || !NICKNAME.matcher(nickname).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid nickname");
		}
		User user = loadUser(userId);
		user.setNickname(nickname);
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
				tastes
		);
	}
}
