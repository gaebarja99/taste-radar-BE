package com.tasteradar.domain.order.api.dto;

/**
 * 사장이 소유한 전체 가게의 리뷰 평균 별점.
 */
public record OwnerRatingSummaryResponse(
		double averageRating,
		long reviewCount,
		long storeCount,
		long fiveStarCount,
		long fourStarCount,
		long threeStarCount,
		long lowStarCount
) {
}
