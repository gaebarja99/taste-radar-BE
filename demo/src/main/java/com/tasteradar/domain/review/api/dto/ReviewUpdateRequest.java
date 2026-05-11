package com.tasteradar.domain.review.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewUpdateRequest(
		@Min(1) @Max(5) int rating,
		@NotBlank String content,
		@NotNull @Valid ReviewTasteDto taste
) {
}
