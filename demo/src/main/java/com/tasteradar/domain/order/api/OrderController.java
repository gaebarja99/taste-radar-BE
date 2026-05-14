package com.tasteradar.domain.order.api;

import com.tasteradar.domain.order.api.dto.OrderCancelRequest;
import com.tasteradar.domain.order.api.dto.OrderCreateRequest;
import com.tasteradar.domain.order.api.dto.OrderDetailResponse;
import com.tasteradar.domain.order.api.dto.OrderSummaryResponse;
import com.tasteradar.domain.order.service.OrderService;
import com.tasteradar.domain.user.entity.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public OrderDetailResponse create(Authentication authentication, @Valid @RequestBody OrderCreateRequest request) {
		return orderService.create(parseUserId(authentication), request);
	}

	@GetMapping("/me")
	public Page<OrderSummaryResponse> myOrders(
			Authentication authentication,
			@PageableDefault(size = 20) Pageable pageable
	) {
		return orderService.myOrders(parseUserId(authentication), pageable);
	}

	@GetMapping("/{orderId}")
	public OrderDetailResponse detail(Authentication authentication, @PathVariable long orderId) {
		return orderService.detail(parseUserId(authentication), parseUserRole(authentication), orderId);
	}

	@PostMapping("/{orderId}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(
			Authentication authentication,
			@PathVariable long orderId,
			@RequestBody(required = false) OrderCancelRequest request
	) {
		String reason = request != null ? request.reason() : null;
		orderService.cancel(parseUserId(authentication), orderId, reason);
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}

	private UserRole parseUserRole(Authentication authentication) {
		boolean isOwner = authentication.getAuthorities().stream()
				.anyMatch(a -> "ROLE_OWNER".equals(a.getAuthority()));
		return isOwner ? UserRole.OWNER : UserRole.CUSTOMER;
	}
}
