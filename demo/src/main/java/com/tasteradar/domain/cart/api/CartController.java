package com.tasteradar.domain.cart.api;

import com.tasteradar.domain.cart.api.dto.CartAddRequest;
import com.tasteradar.domain.cart.api.dto.CartQuantityPatchRequest;
import com.tasteradar.domain.cart.api.dto.CartResponse;
import com.tasteradar.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

	private final CartService cartService;

	@GetMapping
	public CartResponse get(Authentication authentication) {
		return cartService.getCart(parseUserId(authentication));
	}

	@PostMapping("/items")
	public CartResponse add(Authentication authentication, @Valid @RequestBody CartAddRequest request) {
		return cartService.addItem(parseUserId(authentication), request);
	}

	@PatchMapping("/items/{itemId}")
	public CartResponse patchQuantity(
			Authentication authentication,
			@PathVariable long itemId,
			@Valid @RequestBody CartQuantityPatchRequest request
	) {
		return cartService.patchItemQuantity(parseUserId(authentication), itemId, request);
	}

	@DeleteMapping("/items/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeItem(Authentication authentication, @PathVariable long itemId) {
		cartService.removeItem(parseUserId(authentication), itemId);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void clear(Authentication authentication) {
		cartService.clear(parseUserId(authentication));
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
