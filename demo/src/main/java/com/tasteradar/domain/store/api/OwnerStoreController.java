package com.tasteradar.domain.store.api;

import com.tasteradar.domain.store.api.dto.OwnerStoreCreateRequest;
import com.tasteradar.domain.store.api.dto.OwnerStoreSummaryResponse;
import com.tasteradar.domain.store.api.dto.OwnerStoreUpdateRequest;
import com.tasteradar.domain.store.api.dto.StoreCreatedResponse;
import com.tasteradar.domain.store.api.dto.StoreDetailResponse;
import com.tasteradar.domain.store.api.dto.StoreStatusPatchRequest;
import com.tasteradar.domain.store.api.dto.StoreStatusResponse;
import com.tasteradar.domain.store.service.OwnerStoreService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/stores")
public class OwnerStoreController {

	private final OwnerStoreService ownerStoreService;

	/**
	 * 본인이 소유한 모든 가게(폐업 포함) 목록 — 사이드바/관리 화면용.
	 */
	@GetMapping("/mine")
	public List<OwnerStoreSummaryResponse> myStores(Authentication authentication) {
		return ownerStoreService.listMyStores(parseUserId(authentication));
	}

	/**
	 * 본인 가게 상세 — 폐업한 가게도 함께 조회된다.
	 */
	@GetMapping("/{storeId}")
	public StoreDetailResponse myStoreDetail(
			Authentication authentication,
			@PathVariable long storeId
	) {
		return ownerStoreService.getMyStoreDetail(parseUserId(authentication), storeId);
	}

	@PostMapping
	public StoreCreatedResponse create(Authentication authentication, @Valid @RequestBody OwnerStoreCreateRequest request) {
		return new StoreCreatedResponse(ownerStoreService.create(parseUserId(authentication), request).getId());
	}

	@PutMapping("/{storeId}")
	public ResponseEntity<Void> update(
			Authentication authentication,
			@PathVariable long storeId,
			@Valid @RequestBody OwnerStoreUpdateRequest request
	) {
		ownerStoreService.update(parseUserId(authentication), storeId, request);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{storeId}/status")
	public StoreStatusResponse patchStatus(
			Authentication authentication,
			@PathVariable long storeId,
			@Valid @RequestBody StoreStatusPatchRequest request
	) {
		var store = ownerStoreService.patchStatus(parseUserId(authentication), storeId, request);
		return new StoreStatusResponse(store.getStoreStatus());
	}

	/**
	 * 가게 폐업 — 소프트 삭제.
	 * 응답은 204 No Content.
	 */
	@DeleteMapping("/{storeId}")
	public ResponseEntity<Void> closeStore(
			Authentication authentication,
			@PathVariable long storeId
	) {
		ownerStoreService.closeStore(parseUserId(authentication), storeId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 폐업한 가게 재오픈 (is_deleted=false 로 복구). 영업 상태는 PREPARING 으로 초기화.
	 */
	@PostMapping("/{storeId}/reopen")
	public ResponseEntity<Void> reopenStore(
			Authentication authentication,
			@PathVariable long storeId
	) {
		ownerStoreService.reopenStore(parseUserId(authentication), storeId);
		return ResponseEntity.noContent().build();
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
