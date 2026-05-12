package com.tasteradar.domain.store.api;

import com.tasteradar.domain.store.api.dto.OwnerStoreCreateRequest;
import com.tasteradar.domain.store.api.dto.OwnerStoreUpdateRequest;
import com.tasteradar.domain.store.api.dto.StoreCreatedResponse;
import com.tasteradar.domain.store.api.dto.StoreStatusPatchRequest;
import com.tasteradar.domain.store.api.dto.StoreStatusResponse;
import com.tasteradar.domain.store.service.OwnerStoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
