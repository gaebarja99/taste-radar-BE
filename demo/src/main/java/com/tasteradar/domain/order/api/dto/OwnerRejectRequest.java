package com.tasteradar.domain.order.api.dto;

import jakarta.validation.constraints.NotBlank;

public record OwnerRejectRequest(
		@NotBlank String rejectionReason
) {
}
