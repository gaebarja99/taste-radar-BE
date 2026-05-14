package com.tasteradar.domain.review.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewMenuTasteItemDto(
		@NotNull Long menuId,
		@NotBlank String taste
) {
}
