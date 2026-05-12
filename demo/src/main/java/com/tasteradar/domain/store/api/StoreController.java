package com.tasteradar.domain.store.api;

import com.tasteradar.domain.review.api.dto.StoreReviewResponse;
import com.tasteradar.domain.review.service.ReviewService;
import com.tasteradar.domain.store.api.dto.StoreDetailResponse;
import com.tasteradar.domain.store.api.dto.StoreSummaryResponse;
import com.tasteradar.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

	private final StoreService storeService;
	private final ReviewService reviewService;

	/**
	 * 가게명 또는 메뉴명으로 가게 목록 검색 (메뉴 목록 미포함)
	 * - q 미지정/빈값이면 전체 목록
	 */
	@GetMapping
	public Page<StoreSummaryResponse> search(
			@RequestParam(required = false, name = "q") String query,
			@PageableDefault(size = 20) Pageable pageable
	) {
		return storeService.search(query, pageable);
	}

	/**
	 * 내 주변 가게 조회 — Haversine 기반 반경 검색.
	 * - 좌표가 없는 가게는 자동 제외 (가게 등록 시 latitude/longitude 가 함께 저장되어야 노출됩니다)
	 */
	@GetMapping("/nearby")
	public Page<StoreSummaryResponse> nearby(
			@RequestParam double lat,
			@RequestParam double lng,
			@RequestParam(defaultValue = "3") double radiusKm,
			@PageableDefault(size = 20) Pageable pageable
	) {
		return storeService.nearby(lat, lng, radiusKm, pageable);
	}

	@GetMapping("/{storeId}/reviews")
	public Page<StoreReviewResponse> storeReviews(
			@PathVariable long storeId,
			@PageableDefault(size = 20) Pageable pageable
	) {
		return reviewService.pageForStore(storeId, pageable);
	}

	@GetMapping("/{storeId}")
	public StoreDetailResponse detail(@PathVariable long storeId) {
		return storeService.getPublicDetail(storeId);
	}
}

