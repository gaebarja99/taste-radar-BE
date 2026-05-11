package com.tasteradar.domain.menu.api;

import com.tasteradar.domain.menu.api.dto.MenuCreateRequest;
import com.tasteradar.domain.menu.api.dto.MenuResponse;
import com.tasteradar.domain.menu.api.dto.MenuUpdateRequest;
import com.tasteradar.domain.menu.service.MenuCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/stores/{storeId}/menus")
public class OwnerMenuController {

	private final MenuCommandService menuCommandService;

	@PostMapping
	public MenuResponse create(
			Authentication authentication,
			@PathVariable long storeId,
			@Valid @RequestBody MenuCreateRequest request
	) {
		return menuCommandService.create(parseUserId(authentication), storeId, request);
	}

	@PutMapping("/{menuId}")
	public MenuResponse update(
			Authentication authentication,
			@PathVariable long storeId,
			@PathVariable long menuId,
			@Valid @RequestBody MenuUpdateRequest request
	) {
		return menuCommandService.update(parseUserId(authentication), storeId, menuId, request);
	}

	@DeleteMapping("/{menuId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			Authentication authentication,
			@PathVariable long storeId,
			@PathVariable long menuId
	) {
		menuCommandService.delete(parseUserId(authentication), storeId, menuId);
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
