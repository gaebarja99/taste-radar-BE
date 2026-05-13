package com.tasteradar.domain.store.service;

import com.tasteradar.domain.review.repository.ReviewRepository;
import com.tasteradar.domain.review.repository.StoreTasteAggregateProjection;
import com.tasteradar.domain.review.repository.StoreTasteBatchProjection;
import com.tasteradar.domain.store.api.dto.StoreTasteHighlightResponse;
import com.tasteradar.domain.store.api.dto.StoreTasteProfileResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreTasteService {

	public static final int MIN_REVIEWS_FOR_PROFILE = 5;

	private static final List<TasteAxis> AXES = List.of(
			new TasteAxis("sweetness", "단맛"),
			new TasteAxis("saltiness", "짠맛"),
			new TasteAxis("sourness", "신맛"),
			new TasteAxis("bitterness", "쓴맛"),
			new TasteAxis("umami", "감칠맛")
	);

	private final ReviewRepository reviewRepository;

	@Transactional(readOnly = true)
	public StoreTasteProfileResponse getProfile(long storeId, long reviewCount) {
		if (reviewCount < MIN_REVIEWS_FOR_PROFILE) {
			return null;
		}
		return reviewRepository.aggregateTasteForStore(storeId)
				.map(agg -> toProfile(agg, reviewCount))
				.orElse(null);
	}

	@Transactional(readOnly = true)
	public List<StoreTasteHighlightResponse> getHighlights(long storeId, long reviewCount) {
		StoreTasteProfileResponse profile = getProfile(storeId, reviewCount);
		if (profile == null) {
			return List.of();
		}
		return topHighlights(profile, 2);
	}

	@Transactional(readOnly = true)
	public Map<Long, List<StoreTasteHighlightResponse>> getHighlightsBatch(Map<Long, Long> reviewCountByStoreId) {
		List<Long> eligibleIds = reviewCountByStoreId.entrySet().stream()
				.filter(e -> e.getValue() != null && e.getValue() >= MIN_REVIEWS_FOR_PROFILE)
				.map(Map.Entry::getKey)
				.toList();
		if (eligibleIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, StoreTasteProfileResponse> profiles = reviewRepository.aggregateTasteForStores(eligibleIds).stream()
				.collect(Collectors.toMap(
						StoreTasteBatchProjection::getStoreId,
						row -> toProfile(row, reviewCountByStoreId.getOrDefault(row.getStoreId(), 0L))
				));

		Map<Long, List<StoreTasteHighlightResponse>> result = new HashMap<>();
		for (Long storeId : eligibleIds) {
			StoreTasteProfileResponse profile = profiles.get(storeId);
			result.put(storeId, profile == null ? List.of() : topHighlights(profile, 2));
		}
		return result;
	}

	private StoreTasteProfileResponse toProfile(StoreTasteAggregateProjection agg, long reviewCount) {
		return new StoreTasteProfileResponse(
				roundScore(agg.getAvgSweetness()),
				roundScore(agg.getAvgSaltiness()),
				roundScore(agg.getAvgSourness()),
				roundScore(agg.getAvgBitterness()),
				roundScore(agg.getAvgUmami()),
				reviewCount
		);
	}

	private StoreTasteProfileResponse toProfile(StoreTasteBatchProjection agg, long reviewCount) {
		return new StoreTasteProfileResponse(
				roundScore(agg.getAvgSweetness()),
				roundScore(agg.getAvgSaltiness()),
				roundScore(agg.getAvgSourness()),
				roundScore(agg.getAvgBitterness()),
				roundScore(agg.getAvgUmami()),
				reviewCount
		);
	}

	private List<StoreTasteHighlightResponse> topHighlights(StoreTasteProfileResponse profile, int limit) {
		List<StoreTasteHighlightResponse> ranked = new ArrayList<>();
		for (TasteAxis axis : AXES) {
			int score = scoreForKey(profile, axis.key());
			if (score <= 0) {
				continue;
			}
			ranked.add(new StoreTasteHighlightResponse(axis.key(), axis.label(), score));
		}
		ranked.sort(Comparator.comparingInt(StoreTasteHighlightResponse::score).reversed()
				.thenComparing(StoreTasteHighlightResponse::label));
		return ranked.stream().limit(limit).toList();
	}

	private int scoreForKey(StoreTasteProfileResponse profile, String key) {
		return switch (key) {
			case "sweetness" -> profile.sweetness();
			case "saltiness" -> profile.saltiness();
			case "sourness" -> profile.sourness();
			case "bitterness" -> profile.bitterness();
			case "umami" -> profile.umami();
			default -> 0;
		};
	}

	private int roundScore(Double value) {
		if (value == null || value.isNaN()) {
			return 0;
		}
		// boolean 특화 맛 비율(0~1) → 레이더 0~5
		return Math.max(0, Math.min(5, (int) Math.round(value * 5)));
	}

	private record TasteAxis(String key, String label) {
	}
}
