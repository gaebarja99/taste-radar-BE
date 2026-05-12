package com.tasteradar.domain.store.service;

import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.store.api.dto.StoreDetailResponse;
import com.tasteradar.domain.store.api.dto.StoreImageUrlResponse;
import com.tasteradar.domain.store.api.dto.StoreMenuResponse;
import com.tasteradar.domain.store.api.dto.StoreSummaryResponse;
import com.tasteradar.domain.store.entity.Store;
import com.tasteradar.domain.store.repository.StoreRepository;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StoreService {

	private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;

	@Transactional(readOnly = true)
	public Page<StoreSummaryResponse> search(String query, Pageable pageable) {
		return storeRepository.searchByStoreNameOrMenuName(query, pageable)
				.map(this::toSummary);
	}

	/**
	 * 내 주변(반경 km) 가게 조회.
	 * Haversine 공식 기반 native query를 사용하며, 좌표가 없는 가게는 자동 제외됩니다.
	 */
	@Transactional(readOnly = true)
	public Page<StoreSummaryResponse> nearby(double lat, double lng, double radiusKm, Pageable pageable) {
		double radius = Math.max(0.1, Math.min(radiusKm, 50.0));
		return storeRepository.findNearby(lat, lng, radius, pageable)
				.map(this::toSummary);
	}

	@Transactional(readOnly = true)
	public StoreDetailResponse getPublicDetail(long storeId) {
		Store s = storeRepository.findById(storeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (s.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		var images = s.getImages().stream()
				.map(img -> new StoreImageUrlResponse(img.getImgUrl()))
				.toList();
		var menus = menuRepository.findByStoreId(storeId).stream()
				.map(m -> new StoreMenuResponse(
						m.getId(),
						m.getName(),
						m.getPrice(),
						m.getMenuDescription(),
						m.getImageUrl()
				))
				.toList();
		return new StoreDetailResponse(
				s.getId(),
				s.getName(),
				s.getStoreStatus(),
				s.getOpenTime().format(HM),
				s.getCloseTime().format(HM),
				s.getRequiredTimeMinutes(),
				s.getMinOrderAmount(),
				s.getAverageRating(),
				s.getReviewCount(),
				images,
				menus
		);
	}

	private StoreSummaryResponse toSummary(Store s) {
		String thumbnail = Optional.ofNullable(s.getImages())
				.flatMap(images -> images.stream().findFirst())
				.map(img -> img.getImgUrl())
				.orElse(null);
		return new StoreSummaryResponse(
				s.getId(),
				s.getName(),
				s.getStoreStatus(),
				s.getMinOrderAmount(),
				s.getAverageRating(),
				s.getReviewCount(),
				thumbnail,
				s.getLatitude(),
				s.getLongitude()
		);
	}
}

