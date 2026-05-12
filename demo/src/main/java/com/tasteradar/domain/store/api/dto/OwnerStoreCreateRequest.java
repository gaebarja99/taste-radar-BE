package com.tasteradar.domain.store.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record OwnerStoreCreateRequest(
		@NotBlank String name,
		@NotBlank String address,
		@NotBlank String addressDetail,
		@NotNull @Positive int minOrderAmount,
		@NotBlank String openTime,
		@NotBlank String closeTime,
		@NotNull @Positive int requiredTimeMinutes,
		Double latitude,
		Double longitude,
		@Valid @NotEmpty List<OwnerStoreImageRequest> images
) {
	public record OwnerStoreImageRequest(
			@NotBlank String fileName,
			@NotBlank String imgUrl,
			@NotBlank String imgKey
	) {
	}
}
