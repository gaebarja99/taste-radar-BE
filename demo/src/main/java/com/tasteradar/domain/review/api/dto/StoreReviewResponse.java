package com.tasteradar.domain.review.api.dto;

import java.time.Instant;

public record StoreReviewResponse(
		long id,
		int rating,
		String content,
		String ownerReply,
		Instant createdAt
) {
}
