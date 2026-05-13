package com.tasteradar.domain.ai.service.dto;

import java.util.List;

public record LlmMenuRecommendation(
		String message,
		List<Long> menuIds
) {
}
