package com.tasteradar.domain.review.api;

import com.tasteradar.domain.review.api.dto.MyReviewResponse;
import com.tasteradar.domain.review.api.dto.ReviewUpdateRequest;
import com.tasteradar.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@GetMapping("/me")
	public List<MyReviewResponse> myReviews(Authentication authentication) {
		return reviewService.myReviews(parseUserId(authentication));
	}

	@PutMapping("/{reviewId}")
	public MyReviewResponse update(
			@PathVariable long reviewId,
			Authentication authentication,
			@Valid @RequestBody ReviewUpdateRequest request
	) {
		return reviewService.updateMine(parseUserId(authentication), reviewId, request);
	}

	@DeleteMapping("/{reviewId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long reviewId, Authentication authentication) {
		reviewService.deleteMine(parseUserId(authentication), reviewId);
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
