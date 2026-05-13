package com.tasteradar.domain.user.api;

import com.tasteradar.domain.user.api.dto.AddressUpdateRequest;
import com.tasteradar.domain.user.api.dto.NicknameUpdateRequest;
import com.tasteradar.domain.user.api.dto.PasswordUpdateRequest;
import com.tasteradar.domain.user.api.dto.TasteUpdateRequest;
import com.tasteradar.domain.user.api.dto.UserProfileResponse;
import com.tasteradar.domain.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserProfileService userProfileService;

	@GetMapping("/me")
	public UserProfileResponse me(Authentication authentication) {
		return userProfileService.getMe(parseUserId(authentication));
	}

	@PatchMapping("/me/nickname")
	public UserProfileResponse nickname(Authentication authentication, @Valid @RequestBody NicknameUpdateRequest request) {
		return userProfileService.updateNickname(parseUserId(authentication), request.nickname());
	}

	@PutMapping("/me/address")
	public UserProfileResponse address(Authentication authentication, @Valid @RequestBody AddressUpdateRequest request) {
		return userProfileService.updateAddress(
				parseUserId(authentication),
				request.zipCode(),
				request.address(),
				request.addressDetail());
	}

	@PutMapping("/me/password")
	public UserProfileResponse password(Authentication authentication, @Valid @RequestBody PasswordUpdateRequest request) {
		return userProfileService.updatePassword(
				parseUserId(authentication),
				request.currentPassword(),
				request.newPassword());
	}

	@PutMapping("/me/tastes")
	public UserProfileResponse tastes(Authentication authentication, @Valid @RequestBody TasteUpdateRequest request) {
		return userProfileService.updateTastes(
				parseUserId(authentication),
				request.sweet(),
				request.salty(),
				request.sour(),
				request.bitter(),
				request.umami()
		);
	}

	@DeleteMapping("/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void withdraw(Authentication authentication) {
		userProfileService.withdraw(parseUserId(authentication));
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
