package com.tasteradar.domain.menu.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MenuUpdateRequest(
		@NotBlank String name,
		@NotNull @Positive Long price,
		@NotBlank String menuDescription,
		@NotBlank String imageUrl
) {
}
