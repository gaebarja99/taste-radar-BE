package com.tasteradar.domain.order.service;

import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.order.entity.OrderItem;

final class OrderItemMenuSupport {

	private static final String DELETED_LABEL = "삭제된 메뉴";

	private OrderItemMenuSupport() {
	}

	static String name(OrderItem item) {
		if (item.getMenuName() != null && !item.getMenuName().isBlank()) {
			return item.getMenuName().trim();
		}
		Menu menu = item.getMenu();
		if (menu != null) {
			return menu.getName();
		}
		return DELETED_LABEL;
	}

	static long menuId(OrderItem item) {
		Menu menu = item.getMenu();
		if (menu != null) {
			return menu.getId();
		}
		Long id = item.getMenuId();
		return id != null ? id : 0L;
	}
}
