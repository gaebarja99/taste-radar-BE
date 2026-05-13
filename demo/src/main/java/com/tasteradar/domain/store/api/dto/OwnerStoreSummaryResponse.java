package com.tasteradar.domain.store.api.dto;

import com.tasteradar.domain.store.entity.StoreStatus;

/**
 * 사장 사이드바/관리 화면용 본인 가게 요약 — 폐업 가게도 함께 노출되며 {@code isDeleted} 플래그로 구분한다.
 *
 * @param storeId          가게 ID
 * @param storeName        가게명
 * @param storeStatus      영업 상태 (PREPARING/OPEN/CLOSE)
 * @param isDeleted        true 이면 폐업 처리됨
 * @param todayOrderCount  오늘 주문 수 (폐업 가게는 0)
 */
public record OwnerStoreSummaryResponse(
		long storeId,
		String storeName,
		StoreStatus storeStatus,
		boolean isDeleted,
		long todayOrderCount
) {
}
