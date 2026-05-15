package com.tasteradar.security.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank @Email String email,
		@NotBlank
		@Size(min = 8, max = 72)
		@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "영문과 숫자를 포함한 8자 이상")
		String password,
		@NotBlank
		@Size(min = 2, max = 10)
		@Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "한글·영문·숫자 2~10자")
		String nickname,
		@NotBlank @Pattern(regexp = "^(CUSTOMER|OWNER)$", message = "CUSTOMER 또는 OWNER")
		String role
) {
}
