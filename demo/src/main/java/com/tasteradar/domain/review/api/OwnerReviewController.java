package com.tasteradar.domain.review.api;

import com.tasteradar.domain.review.api.dto.OwnerReplyRequest;
import com.tasteradar.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/reviews")
public class OwnerReviewController {

	private final ReviewService reviewService;

	@PostMapping("/{reviewId}/reply")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reply(
			@PathVariable long reviewId,
			Authentication authentication,
			@Valid @RequestBody OwnerReplyRequest request
	) {
		reviewService.ownerReply(parseUserId(authentication), reviewId, request);
	}

	/**
	 * 사장이 자기 가게의 리뷰를 삭제(소프트).
	 */
	@DeleteMapping("/{reviewId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable long reviewId,
			Authentication authentication
	) {
		reviewService.deleteByOwner(parseUserId(authentication), reviewId);
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
