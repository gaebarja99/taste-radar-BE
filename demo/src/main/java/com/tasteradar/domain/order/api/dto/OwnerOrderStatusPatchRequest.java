package com.tasteradar.domain.order.api.dto;

import com.tasteradar.domain.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OwnerOrderStatusPatchRequest(
		@NotNull OrderStatus status
) {
}
