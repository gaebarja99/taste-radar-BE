package com.tasteradar.domain.menu.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuUpdateRequest(
		@NotBlank String name,
		@Min(1) long price,
		@JsonAlias("description")
		@Size(max = 2000) String menuDescription,
		@JsonAlias({"imgUrl", "image"})
		@Size(max = 2048) String imageUrl
) {
}
