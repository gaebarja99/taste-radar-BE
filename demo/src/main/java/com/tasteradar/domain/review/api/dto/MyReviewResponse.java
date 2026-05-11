package com.tasteradar.domain.review.api.dto;

import java.time.Instant;

public record MyReviewResponse(
		long id,
		long orderId,
		long storeId,
		String storeName,
		int rating,
		String content,
		ReviewTasteDto taste,
		String ownerReply,
		Instant createdAt
) {
}
