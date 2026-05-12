package com.tasteradar.domain.notification.api.dto;

import com.tasteradar.domain.order.entity.OrderStatus;
import java.time.Instant;

public record NotificationResponse(
		long id,
		long orderId,
		OrderStatus orderStatus,
		String message,
		boolean read,
		Instant createdAt
) {
}
