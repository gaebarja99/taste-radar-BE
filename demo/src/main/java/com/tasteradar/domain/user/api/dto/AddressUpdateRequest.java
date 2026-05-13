package com.tasteradar.domain.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressUpdateRequest(
		@NotBlank @Size(max = 10) String zipCode,
		@NotBlank @Size(max = 200) String address,
		@NotBlank @Size(max = 100) String addressDetail
) {
}
