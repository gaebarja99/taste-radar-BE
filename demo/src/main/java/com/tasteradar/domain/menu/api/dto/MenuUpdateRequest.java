package com.tasteradar.domain.menu.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MenuUpdateRequest(
		@NotBlank String name,
		@NotNull @Positive Long price,
		@Size(max = 2000) String menuDescription,
		@Size(max = 1000) String imageUrl
) {
}
