package com.tasteradar.domain.review.api;

import com.tasteradar.domain.review.api.dto.MyReviewResponse;
import com.tasteradar.domain.review.api.dto.ReviewCreateRequest;
import com.tasteradar.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderReviewController {

	private final ReviewService reviewService;

	@PostMapping("/{orderId}/reviews")
	public MyReviewResponse create(
			@PathVariable long orderId,
			Authentication authentication,
			@Valid @RequestBody ReviewCreateRequest request
	) {
		return reviewService.createForOrder(parseUserId(authentication), orderId, request);
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
