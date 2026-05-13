package com.tasteradar.domain.order.service;

import com.tasteradar.domain.order.api.dto.OrderSummaryResponse;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderItem;
import java.util.stream.Collectors;

final class OrderSummaryMapper {

	private OrderSummaryMapper() {
	}

	static OrderSummaryResponse toSummary(FoodOrder order) {
		return new OrderSummaryResponse(
				order.getId(),
				order.getStore().getId(),
				order.getStore().getName(),
				buildMenuSummary(order),
				order.getOrderStatus(),
				order.getTotalAmount(),
				order.getRejectionReason(),
				order.getCreatedAt()
		);
	}

	private static String buildMenuSummary(FoodOrder order) {
		if (order.getItems() == null || order.getItems().isEmpty()) {
			return "메뉴 정보 없음";
		}
		return order.getItems().stream()
				.map(OrderSummaryMapper::formatItemLine)
				.collect(Collectors.joining(", "));
	}

	private static String formatItemLine(OrderItem item) {
		String name = item.getMenu().getName();
		int qty = item.getQuantity();
		return qty > 1 ? name + " ×" + qty : name;
	}
}
