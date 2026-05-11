package com.tasteradar.domain.review.api.dto;

import jakarta.validation.constraints.NotBlank;

public record OwnerReplyRequest(
		@NotBlank String ownerReply
) {
}
