package com.tasteradar.domain.order.api.dto;

import com.tasteradar.domain.order.entity.OrderStatus;
import java.time.Instant;
import java.util.List;

public record OrderDetailResponse(
		long id,
		long storeId,
		String storeName,
		OrderStatus orderStatus,
		String zipCode,
		String address,
		String addressDetail,
		int totalAmount,
		String rejectionReason,
		Instant createdAt,
		List<OrderItemResponse> items
) {
}
