package com.tasteradar.domain.order.api.dto;

import jakarta.validation.constraints.Size;

public record OrderCancelRequest(
		@Size(max = 200) String reason
) {
}
