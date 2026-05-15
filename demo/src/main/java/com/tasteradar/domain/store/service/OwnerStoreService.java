package com.tasteradar.domain.store.service;

import com.tasteradar.domain.order.repository.FoodOrderRepository;
import com.tasteradar.domain.store.api.dto.OwnerStoreCreateRequest;
import com.tasteradar.domain.store.api.dto.OwnerStoreSummaryResponse;
import com.tasteradar.domain.store.api.dto.OwnerStoreUpdateRequest;
import com.tasteradar.domain.store.api.dto.StoreDetailResponse;
import com.tasteradar.domain.store.api.dto.StoreImageUrlResponse;
import com.tasteradar.domain.store.api.dto.StoreMenuResponse;
import com.tasteradar.domain.store.api.dto.StoreStatusPatchRequest;
import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.store.entity.Store;
import com.tasteradar.domain.store.entity.StoreImage;
import com.tasteradar.domain.store.entity.StoreStatus;
import com.tasteradar.domain.store.repository.StoreRepository;
import com.tasteradar.domain.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OwnerStoreService {

	private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private final StoreRepository storeRepository;
	private final UserRepository userRepository;
	private final MenuRepository menuRepository;
	private final FoodOrderRepository foodOrderRepository;

	/**
	 * 본인이 소유한 모든 가게(폐업 포함) 목록 + 오늘 주문 수.
	 */
	@Transactional(readOnly = true)
	public List<OwnerStoreSummaryResponse> listMyStores(long ownerId) {
		List<Store> stores = storeRepository.findByOwner_IdOrderByIdAsc(ownerId);
		Instant start = LocalDate.now(SEOUL).atStartOfDay(SEOUL).toInstant();
		Instant end = LocalDate.now(SEOUL).plusDays(1).atStartOfDay(SEOUL).toInstant();
		Map<Long, Long> todayMap = foodOrderRepository.countTodayGroupedByStoreNative(ownerId, start, end).stream()
				.collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[2]).longValue()));
		return stores.stream()
				.map(s -> new OwnerStoreSummaryResponse(
						s.getId(),
						s.getName(),
						s.getStoreStatus(),
						s.isDeleted(),
						todayMap.getOrDefault(s.getId(), 0L)
				))
				.toList();
	}

	/**
	 * 본인 가게 상세 — 폐업된 가게도 함께 조회된다(사이드바에서 클릭해 정보를 확인할 수 있게).
	 */
	@Transactional(readOnly = true)
	public StoreDetailResponse getMyStoreDetail(long ownerId, long storeId) {
		Store s = storeRepository.findByIdAndOwner_Id(storeId, ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
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
				s.getAddress(),
				s.getAddressDetail(),
				s.getOpenTime().format(HM),
				s.getCloseTime().format(HM),
				s.getRequiredTimeMinutes(),
				s.getMinOrderAmount(),
				s.getAverageRating(),
				s.getReviewCount(),
				s.getLatitude(),
				s.getLongitude(),
				images,
				menus,
				null
		);
	}

	/**
	 * 폐업된 가게를 다시 영업 가능 상태로 복구.
	 * 본인 가게여야 하며, is_deleted=false 로 되돌리고 상태는 PREPARING 으로 초기화한다.
	 */
	@Transactional
	public void reopenStore(long ownerId, long storeId) {
		Store store = storeRepository.findByIdAndOwner_Id(storeId, ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (!store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Store is not closed");
		}
		store.setDeleted(false);
		store.setStoreStatus(StoreStatus.PREPARING);
	}

	@Transactional
	public Store create(long ownerId, OwnerStoreCreateRequest request) {
		var owner = userRepository.findById(ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		Store store = new Store();
		store.setOwner(owner);
		store.setName(request.name());
		store.setAddress(request.address());
		store.setAddressDetail(request.addressDetail());
		store.setLatitude(request.latitude());
		store.setLongitude(request.longitude());
		store.setMinOrderAmount(request.minOrderAmount());
		store.setOpenTime(LocalTime.parse(request.openTime()));
		store.setCloseTime(LocalTime.parse(request.closeTime()));
		store.setRequiredTimeMinutes(request.requiredTimeMinutes());
		store.setStoreStatus(StoreStatus.PREPARING);
		store.setDeleted(false);
		store.setAverageRating(0);
		store.setReviewCount(0);
		for (OwnerStoreCreateRequest.OwnerStoreImageRequest img : request.images()) {
			StoreImage si = new StoreImage();
			si.setStore(store);
			si.setFileName(img.fileName());
			si.setImgUrl(img.imgUrl());
			si.setImgKey(img.imgKey());
			store.getImages().add(si);
		}
		return storeRepository.save(store);
	}

	@Transactional
	public Store update(long ownerId, long storeId, OwnerStoreUpdateRequest request) {
		Store store = storeRepository.findByIdAndOwner_Id(storeId, ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		store.setName(request.name());
		store.setAddress(request.address());
		store.setAddressDetail(request.addressDetail());
		store.setMinOrderAmount(request.minOrderAmount());
		store.setOpenTime(LocalTime.parse(request.openTime()));
		store.setCloseTime(LocalTime.parse(request.closeTime()));
		store.setRequiredTimeMinutes(request.requiredTimeMinutes());
		store.setLatitude(request.latitude());
		store.setLongitude(request.longitude());

		if (request.images() != null && !request.images().isEmpty()) {
			store.getImages().clear();
			for (OwnerStoreCreateRequest.OwnerStoreImageRequest img : request.images()) {
				StoreImage si = new StoreImage();
				si.setStore(store);
				si.setFileName(img.fileName());
				si.setImgUrl(img.imgUrl());
				si.setImgKey(img.imgKey());
				store.getImages().add(si);
			}
		}
		return store;
	}

	@Transactional
	public Store patchStatus(long ownerId, long storeId, StoreStatusPatchRequest request) {
		Store store = storeRepository.findByIdAndOwner_Id(storeId, ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		store.setStoreStatus(request.status());
		return store;
	}

	/**
	 * 가게 폐업(소프트 삭제).
	 * - {@link Store} 의 {@code @SQLDelete} 가 is_deleted=true UPDATE 를 수행한다.
	 * - 폐업 후에는 영업 상태도 CLOSE 로 함께 변경한다.
	 */
	@Transactional
	public void closeStore(long ownerId, long storeId) {
		Store store = storeRepository.findByIdAndOwner_Id(storeId, ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		store.setStoreStatus(StoreStatus.CLOSE);
		storeRepository.delete(store);
	}
}
