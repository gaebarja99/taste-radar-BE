package com.tasteradar.domain.order.api.dto;

import java.time.LocalDate;

/**
 * 일별 매출 집계 (배달 완료 주문 기준).
 */
public record DailySalesPointDto(
		LocalDate date,
		long salesAmount,
		long orderCount
) {
}
