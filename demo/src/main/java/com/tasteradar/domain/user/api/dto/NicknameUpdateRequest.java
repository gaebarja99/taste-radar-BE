package com.tasteradar.domain.user.api.dto;

import jakarta.validation.constraints.NotBlank;

public record NicknameUpdateRequest(
		@NotBlank String nickname
) {
}
