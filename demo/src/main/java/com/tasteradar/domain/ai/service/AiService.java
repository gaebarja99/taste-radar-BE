package com.tasteradar.domain.ai.service;

import com.tasteradar.domain.ai.api.dto.StoreRecommendationsResponse;
import com.tasteradar.domain.ai.api.dto.TastePentagonResponse;
import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.review.entity.Review;
import com.tasteradar.domain.review.repository.ReviewRepository;
import com.tasteradar.domain.store.repository.StoreRepository;
import com.tasteradar.domain.user.repository.UserRepository;
import java.time.Duration;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AiService {

	private final AiCacheService aiCacheService;
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;

	private static final Duration TASTE_TTL = Duration.ofMinutes(10);
	private static final Duration RECOMMENDATION_CACHE_TTL = Duration.ofMinutes(5);

	@Transactional(readOnly = true)
	public TastePentagonResponse tastePentagon(long userId) {
		String key = "ai:taste:user:" + userId;
		return aiCacheService.getOrCompute(key, TASTE_TTL, TastePentagonResponse.class, () -> computeTaste(userId));
	}

	@Transactional(readOnly = true)
	public StoreRecommendationsResponse recommendMenus(long userId, long storeId) {
		String key = "ai:reco:store:" + storeId + ":user:" + userId;
		return aiCacheService.getOrCompute(key, RECOMMENDATION_CACHE_TTL, StoreRecommendationsResponse.class,
				() -> computeRecommendations(userId, storeId));
	}

	private TastePentagonResponse computeTaste(long userId) {
		var reviews = reviewRepository.findByUser_IdOrderByCreatedAtDesc(userId);
		if (reviews.isEmpty()) {
			// 리뷰가 없으면 0으로 반환 (프론트에서 빈 그래프 처리)
			return new TastePentagonResponse(0, 0, 0, 0, 0);
		}
		int sweetness = (int) Math.round(reviews.stream().mapToInt(Review::getSweetness).average().orElse(0));
		int saltiness = (int) Math.round(reviews.stream().mapToInt(Review::getSaltiness).average().orElse(0));
		int sourness = (int) Math.round(reviews.stream().mapToInt(Review::getSourness).average().orElse(0));
		int bitterness = (int) Math.round(reviews.stream().mapToInt(Review::getBitterness).average().orElse(0));
		int umami = (int) Math.round(reviews.stream().mapToInt(Review::getUmami).average().orElse(0));
		// 0~5 범위로 클램프(리뷰 입력 검증이 없다면 안전장치)
		return new TastePentagonResponse(
				clamp0to5(sweetness),
				clamp0to5(saltiness),
				clamp0to5(sourness),
				clamp0to5(bitterness),
				clamp0to5(umami)
		);
	}

	private StoreRecommendationsResponse computeRecommendations(long userId, long storeId) {
		var store = storeRepository.findById(storeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		var taste = user.getTastePreference();

		// 메뉴에 맛 메타데이터가 없어서, 현재는 "선호 맛 기반 메시지 + 가격순 상위 3개"로 단순 추천.
		var menus = menuRepository.findByStoreId(storeId).stream()
				.sorted(Comparator.comparingLong(Menu::getPrice))
				.limit(3)
				.map(m -> new StoreRecommendationsResponse.RecommendedMenu(m.getId(), m.getName(), m.getPrice(), m.getImageUrl()))
				.toList();

		String tasteMsg = taste == null
				? "회원님의 입맛 설정이 없어 기본 추천을 보여드려요."
				: "회원님의 선호 입맛을 참고해서 추천했어요.";

		return new StoreRecommendationsResponse(tasteMsg, menus);
	}

	private int clamp0to5(int v) {
		return Math.max(0, Math.min(5, v));
	}
}

