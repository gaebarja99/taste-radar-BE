package com.tasteradar.domain.store.service;

import com.tasteradar.domain.store.api.dto.OwnerStoreCreateRequest;
import com.tasteradar.domain.store.api.dto.StoreStatusPatchRequest;
import com.tasteradar.domain.store.entity.Store;
import com.tasteradar.domain.store.entity.StoreImage;
import com.tasteradar.domain.store.entity.StoreStatus;
import com.tasteradar.domain.store.repository.StoreRepository;
import com.tasteradar.domain.user.repository.UserRepository;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OwnerStoreService {

	private final StoreRepository storeRepository;
	private final UserRepository userRepository;

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
	public Store patchStatus(long ownerId, long storeId, StoreStatusPatchRequest request) {
		Store store = storeRepository.findByIdAndOwner_Id(storeId, ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		store.setStoreStatus(request.status());
		return store;
	}
}
