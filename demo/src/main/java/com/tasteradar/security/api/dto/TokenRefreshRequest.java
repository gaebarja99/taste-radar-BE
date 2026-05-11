package com.tasteradar.security.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
		@NotBlank String refreshToken
) {
}

