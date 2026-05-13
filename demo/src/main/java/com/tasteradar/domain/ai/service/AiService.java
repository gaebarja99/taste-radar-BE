package com.tasteradar.domain.ai.service;

import com.tasteradar.domain.ai.api.dto.StoreRecommendationsResponse;
import com.tasteradar.domain.ai.api.dto.TastePentagonResponse;
import com.tasteradar.domain.ai.service.dto.LlmMenuRecommendation;
import com.tasteradar.domain.ai.service.dto.MenuRecommendContext;
import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.review.entity.Review;
import com.tasteradar.domain.review.repository.ReviewRepository;
import com.tasteradar.domain.store.api.dto.StoreTasteProfileResponse;
import com.tasteradar.domain.store.entity.Store;
import com.tasteradar.domain.store.repository.StoreRepository;
import com.tasteradar.domain.store.service.StoreTasteService;
import com.tasteradar.domain.user.entity.UserTastePreference;
import com.tasteradar.domain.user.repository.UserRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

	private final AiCacheService aiCacheService;
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;
	private final StoreTasteService storeTasteService;
	private final GeminiRecommendationClient geminiRecommendationClient;

	private static final Duration TASTE_TTL = Duration.ofMinutes(10);
	private static final Duration RECOMMENDATION_CACHE_TTL = Duration.ofMinutes(5);
	private static final int STORE_TASTE_MATCH_THRESHOLD = 3;

	@Transactional(readOnly = true)
	public TastePentagonResponse tastePentagon(long userId) {
		String key = "ai:taste:user:" + userId;
		return aiCacheService.getOrCompute(key, TASTE_TTL, TastePentagonResponse.class, () -> computeTaste(userId));
	}

	@Transactional(readOnly = true)
	public StoreRecommendationsResponse recommendMenus(long userId, long storeId) {
		String key = "ai:reco:v6:store:" + storeId + ":user:" + userId;
		return aiCacheService.getOrCompute(key, RECOMMENDATION_CACHE_TTL, StoreRecommendationsResponse.class,
				() -> computeRecommendations(userId, storeId));
	}

	private TastePentagonResponse computeTaste(long userId) {
		var reviews = reviewRepository.findByUser_IdOrderByCreatedAtDesc(userId);
		if (reviews.isEmpty()) {
			return new TastePentagonResponse(0, 0, 0, 0, 0);
		}
		return new TastePentagonResponse(
				ratioToScore(reviews.stream().filter(Review::isSweetness).count(), reviews.size()),
				ratioToScore(reviews.stream().filter(Review::isSaltiness).count(), reviews.size()),
				ratioToScore(reviews.stream().filter(Review::isSourness).count(), reviews.size()),
				ratioToScore(reviews.stream().filter(Review::isBitterness).count(), reviews.size()),
				ratioToScore(reviews.stream().filter(Review::isUmami).count(), reviews.size())
		);
	}

	private StoreRecommendationsResponse computeRecommendations(long userId, long storeId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		UserTastePreference taste = user.getTastePreference();
		StoreTasteProfileResponse storeProfile = storeTasteService.getProfile(storeId, store.getReviewCount());
		List<Menu> menus = menuRepository.findByStoreId(storeId);

		if (geminiRecommendationClient.isConfigured()) {
			MenuRecommendContext context = new MenuRecommendContext(
					store.getName(),
					taste,
					storeProfile,
					menus.stream()
							.map(m -> new MenuRecommendContext.MenuCandidate(
									m.getId(),
									m.getName(),
									m.getMenuDescription(),
									m.getPrice()))
							.toList()
			);
			Optional<LlmMenuRecommendation> llm = geminiRecommendationClient.recommend(context);
			if (llm.isPresent()) {
				List<StoreRecommendationsResponse.RecommendedMenu> picked = mapValidatedMenus(llm.get(), menus);
				if (!picked.isEmpty()) {
					log.info("Gemini menu recommendation used for storeId={} userId={}", storeId, userId);
					return new StoreRecommendationsResponse(llm.get().message(), picked, "AI");
				}
				log.warn("Gemini returned menuIds but none matched store menus for storeId={}", storeId);
			}
		} else {
			log.debug("Gemini skipped (no API key) for storeId={} userId={}", storeId, userId);
		}

		return ruleBasedRecommendations(taste, storeProfile, menus, userId);
	}

	private List<StoreRecommendationsResponse.RecommendedMenu> mapValidatedMenus(
			LlmMenuRecommendation llm,
			List<Menu> menus
	) {
		Map<Long, Menu> menuById = new LinkedHashMap<>();
		for (Menu menu : menus) {
			menuById.put(menu.getId(), menu);
		}
		List<StoreRecommendationsResponse.RecommendedMenu> result = new ArrayList<>();
		for (Long menuId : llm.menuIds()) {
			if (result.size() >= 3) {
				break;
			}
			Menu menu = menuById.get(menuId);
			if (menu == null) {
				continue;
			}
			result.add(new StoreRecommendationsResponse.RecommendedMenu(
					menu.getId(),
					menu.getName(),
					menu.getPrice(),
					menu.getImageUrl()));
		}
		return result;
	}

	private StoreRecommendationsResponse ruleBasedRecommendations(
			UserTastePreference taste,
			StoreTasteProfileResponse storeProfile,
			List<Menu> menus,
			long userId
	) {
		var picked = pickMenus(menus, userId, 3).stream()
				.map(m -> new StoreRecommendationsResponse.RecommendedMenu(
						m.getId(), m.getName(), m.getPrice(), m.getImageUrl()))
				.toList();
		String tasteMsg = buildRecommendationMessage(taste, storeProfile);
		return new StoreRecommendationsResponse(tasteMsg, picked, "RULE");
	}

	private String buildRecommendationMessage(UserTastePreference taste, StoreTasteProfileResponse storeProfile) {
		if (taste == null || !hasAnyPreference(taste)) {
			return "입맛 설정을 하시면 이 가게 메뉴를 더 잘 추천해 드릴 수 있어요.";
		}
		List<String> preferred = preferredLabels(taste);
		if (storeProfile == null) {
			return "회원님이 좋아하시는 " + String.join(", ", preferred)
					+ " 기준으로 메뉴를 골랐어요. 가게 맛 프로필은 리뷰가 더 쌓이면 정확해져요.";
		}
		List<String> matched = matchingLabels(taste, storeProfile);
		if (matched.isEmpty()) {
			return "이 가게는 " + String.join(", ", storeStrongLabels(storeProfile))
					+ " 쪽으로 리뷰가 많아요. 취향과 비교해 보시고 아래 메뉴를 참고해 보세요.";
		}
		return "회원님이 선호하시는 " + String.join(", ", matched)
				+ "과(와) 이 가게 리뷰 맛이 잘 맞는 편이에요. 아래 메뉴를 추천해요.";
	}

	private List<String> matchingLabels(UserTastePreference taste, StoreTasteProfileResponse profile) {
		List<String> matched = new ArrayList<>();
		if (taste.isSweet() && profile.sweetness() >= STORE_TASTE_MATCH_THRESHOLD) {
			matched.add("단맛");
		}
		if (taste.isSalty() && profile.saltiness() >= STORE_TASTE_MATCH_THRESHOLD) {
			matched.add("짠맛");
		}
		if (taste.isSour() && profile.sourness() >= STORE_TASTE_MATCH_THRESHOLD) {
			matched.add("신맛");
		}
		if (taste.isBitter() && profile.bitterness() >= STORE_TASTE_MATCH_THRESHOLD) {
			matched.add("쓴맛");
		}
		if (taste.isUmami() && profile.umami() >= STORE_TASTE_MATCH_THRESHOLD) {
			matched.add("감칠맛");
		}
		return matched;
	}

	private List<String> storeStrongLabels(StoreTasteProfileResponse profile) {
		List<String> labels = new ArrayList<>();
		if (profile.sweetness() >= STORE_TASTE_MATCH_THRESHOLD) {
			labels.add("단맛");
		}
		if (profile.saltiness() >= STORE_TASTE_MATCH_THRESHOLD) {
			labels.add("짠맛");
		}
		if (profile.sourness() >= STORE_TASTE_MATCH_THRESHOLD) {
			labels.add("신맛");
		}
		if (profile.bitterness() >= STORE_TASTE_MATCH_THRESHOLD) {
			labels.add("쓴맛");
		}
		if (profile.umami() >= STORE_TASTE_MATCH_THRESHOLD) {
			labels.add("감칠맛");
		}
		if (labels.isEmpty()) {
			labels.add("균형");
		}
		return labels;
	}

	private List<String> preferredLabels(UserTastePreference taste) {
		List<String> labels = new ArrayList<>();
		if (taste.isSweet()) {
			labels.add("단맛");
		}
		if (taste.isSalty()) {
			labels.add("짠맛");
		}
		if (taste.isSour()) {
			labels.add("신맛");
		}
		if (taste.isBitter()) {
			labels.add("쓴맛");
		}
		if (taste.isUmami()) {
			labels.add("감칠맛");
		}
		return labels;
	}

	private boolean hasAnyPreference(UserTastePreference taste) {
		return taste.isSweet() || taste.isSalty() || taste.isSour() || taste.isBitter() || taste.isUmami();
	}

	private List<Menu> pickMenus(List<Menu> menus, long userId, int limit) {
		if (menus.isEmpty()) {
			return List.of();
		}
		int skip = (int) (userId % Math.max(1, menus.size() / 2));
		return menus.stream()
				.sorted(Comparator.comparingLong(Menu::getPrice))
				.skip(Math.min(skip, Math.max(0, menus.size() - limit)))
				.limit(limit)
				.toList();
	}

	private int ratioToScore(long count, int total) {
		if (total <= 0) {
			return 0;
		}
		return Math.max(0, Math.min(5, (int) Math.round((count * 5.0) / total)));
	}
}
