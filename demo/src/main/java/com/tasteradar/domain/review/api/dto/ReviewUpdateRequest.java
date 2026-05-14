package com.tasteradar.domain.review.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReviewUpdateRequest(
		@Min(1) @Max(5) int rating,
		@NotBlank String content,
		@NotEmpty @Valid List<ReviewMenuTasteItemDto> menuTastes
) {
}
