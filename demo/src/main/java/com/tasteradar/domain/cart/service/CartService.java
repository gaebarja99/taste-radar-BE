package com.tasteradar.domain.cart.service;

import com.tasteradar.domain.cart.api.dto.CartAddRequest;
import com.tasteradar.domain.cart.api.dto.CartItemResponse;
import com.tasteradar.domain.cart.api.dto.CartQuantityPatchRequest;
import com.tasteradar.domain.cart.api.dto.CartResponse;
import com.tasteradar.domain.cart.entity.Cart;
import com.tasteradar.domain.cart.entity.CartItem;
import com.tasteradar.domain.cart.repository.CartRepository;
import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.store.repository.StoreRepository;
import com.tasteradar.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;

	@Transactional(readOnly = true)
	public CartResponse getCart(long userId) {
		return cartRepository.findByUser_Id(userId)
				.map(this::toDto)
				.orElse(new CartResponse(null, null, List.of()));
	}

	@Transactional
	public CartResponse addItem(long userId, CartAddRequest request) {
		var store = storeRepository.findById(request.storeId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		Menu menu = menuRepository.findByIdAndStore_Id(request.menuId(), request.storeId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found"));
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		Cart cart = cartRepository.findByUser_Id(userId).orElse(null);
		if (cart == null) {
			cart = new Cart();
			cart.setUser(user);
			cart.setStore(store);
			cartRepository.save(cart);
		} else if (cart.getStore().getId() != request.storeId()) {
			cart.getItems().clear();
			cart.setStore(store);
		}

		Optional<CartItem> same = cart.getItems().stream()
				.filter(ci -> ci.getMenu().getId().equals(menu.getId()))
				.findFirst();
		if (same.isPresent()) {
			CartItem ci = same.get();
			ci.setQuantity(ci.getQuantity() + request.quantity());
		} else {
			CartItem ci = new CartItem();
			ci.setCart(cart);
			ci.setMenu(menu);
			ci.setQuantity(request.quantity());
			cart.getItems().add(ci);
		}
		return toDto(cart);
	}

	@Transactional
	public CartResponse patchItemQuantity(long userId, long itemId, CartQuantityPatchRequest request) {
		Cart cart = cartRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
		CartItem item = cart.getItems().stream()
				.filter(ci -> ci.getId().equals(itemId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
		item.setQuantity(request.quantity());
		return toDto(cart);
	}

	@Transactional
	public void removeItem(long userId, long itemId) {
		Cart cart = cartRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
		CartItem item = cart.getItems().stream()
				.filter(ci -> ci.getId().equals(itemId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
		cart.getItems().remove(item);
	}

	@Transactional
	public void clear(long userId) {
		cartRepository.findByUser_Id(userId).ifPresent(c -> {
			c.getItems().clear();
		});
	}

	private CartResponse toDto(Cart cart) {
		List<CartItemResponse> items = cart.getItems().stream()
				.map(ci -> new CartItemResponse(
						ci.getId(),
						ci.getMenu().getId(),
						ci.getMenu().getName(),
						ci.getMenu().getPrice(),
						ci.getQuantity()
				))
				.toList();
		return new CartResponse(cart.getStore().getId(), cart.getStore().getName(), items);
	}
}
