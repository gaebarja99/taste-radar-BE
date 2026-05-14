package com.tasteradar.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
	PENDING("주문 확인"),
	COOKING("조리 중"),
	DELIVERING("배달 중"),
	DELIVERED("배달 완료"),
	REJECTED("주문 거절"),
	CANCELED("주문 취소");

	private final String description;
}

