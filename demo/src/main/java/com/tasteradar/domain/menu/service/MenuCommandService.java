package com.tasteradar.domain.menu.service;

import com.tasteradar.domain.menu.api.dto.MenuCreateRequest;
import com.tasteradar.domain.menu.api.dto.MenuResponse;
import com.tasteradar.domain.menu.api.dto.MenuUpdateRequest;
import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MenuCommandService {

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;

	@Transactional
	public MenuResponse create(long ownerId, long storeId, MenuCreateRequest request) {
		var store = storeRepository.findByIdAndOwner_Id(storeId, ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		Menu menu = new Menu();
		menu.setStore(store);
		menu.setName(request.name());
		menu.setPrice(request.price());
		menu.setMenuDescription(nullSafe(request.menuDescription()));
		menu.setImageUrl(nullSafe(request.imageUrl()));
		menu.setDeleted(false);
		menuRepository.save(menu);
		return toResponse(menu);
	}

	@Transactional
	public MenuResponse update(long ownerId, long storeId, long menuId, MenuUpdateRequest request) {
		Menu menu = menuRepository.findByIdAndStore_Id(menuId, storeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found"));
		assertOwner(menu, ownerId);
		menu.setName(request.name());
		menu.setPrice(request.price());
		menu.setMenuDescription(nullSafe(request.menuDescription()));
		menu.setImageUrl(nullSafe(request.imageUrl()));
		return toResponse(menu);
	}

	private String nullSafe(String s) {
		return s == null ? "" : s.trim();
	}

	@Transactional
	public void delete(long ownerId, long storeId, long menuId) {
		Menu menu = menuRepository.findByIdAndStore_Id(menuId, storeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found"));
		assertOwner(menu, ownerId);
		menuRepository.delete(menu);
	}

	private void assertOwner(Menu menu, long ownerId) {
		if (menu.getStore().getOwner().getId() != ownerId) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
		}
		if (menu.getStore().isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
	}

	private MenuResponse toResponse(Menu m) {
		return new MenuResponse(m.getId(), m.getName(), m.getPrice(), m.getMenuDescription(), m.getImageUrl());
	}
}
