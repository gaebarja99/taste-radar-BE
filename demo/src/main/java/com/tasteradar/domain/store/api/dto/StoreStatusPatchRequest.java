package com.tasteradar.domain.store.api.dto;

import com.tasteradar.domain.store.entity.StoreStatus;
import jakarta.validation.constraints.NotNull;

public record StoreStatusPatchRequest(
		@NotNull StoreStatus status
) {
}
