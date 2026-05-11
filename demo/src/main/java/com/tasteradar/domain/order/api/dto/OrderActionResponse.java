package com.tasteradar.domain.order.api.dto;

import com.tasteradar.domain.order.entity.OrderStatus;

public record OrderActionResponse(long orderId, OrderStatus status) {
}
