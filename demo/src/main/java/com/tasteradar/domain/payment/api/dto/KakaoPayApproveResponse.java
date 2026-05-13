package com.tasteradar.domain.payment.api.dto;

import java.time.Instant;

public record KakaoPayApproveResponse(
		Long orderId,
		String status,
		int totalAmount,
		Instant approvedAt
) {
}
