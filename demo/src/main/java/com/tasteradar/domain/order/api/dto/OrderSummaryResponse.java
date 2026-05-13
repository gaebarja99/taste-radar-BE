package com.tasteradar.domain.order.api.dto;

import com.tasteradar.domain.order.entity.OrderStatus;
import java.time.Instant;

public record OrderSummaryResponse(
		long id,
		long storeId,
		String storeName,
		String menuSummary,
		OrderStatus orderStatus,
		int totalAmount,
		String rejectionReason,
		Instant createdAt
) {
}
