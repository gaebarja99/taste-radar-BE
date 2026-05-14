package com.tasteradar.domain.review.api.dto;

import java.time.Instant;
import java.util.List;

public record MyReviewResponse(
		long id,
		long orderId,
		long storeId,
		String storeName,
		int rating,
		String content,
		ReviewTasteDto taste,
		List<ReviewMenuTasteResponse> menuTastes,
		String ownerReply,
		Instant createdAt
) {
}
