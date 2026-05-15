package com.tasteradar.domain.order.api.dto;

import java.util.List;

/**
 * 이번 주(월~일) 일별 매출 + 지난 주 대비 변화율.
 */
public record WeeklySalesResponse(
		List<DailySalesPointDto> days,
		long thisWeekTotal,
		long lastWeekTotal,
		double changePercent
) {
}
