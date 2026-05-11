package com.tasteradar.domain.order.api.dto;

import com.tasteradar.domain.order.entity.OrderStatus;
import java.time.Instant;

public record OrderSummaryResponse(
		long id,
		String storeName,
		OrderStatus orderStatus,
		int totalAmount,
		Instant createdAt
) {
}
