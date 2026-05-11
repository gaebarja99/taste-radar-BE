package com.tasteradar.domain.order.api;

import com.tasteradar.domain.order.api.dto.OrderActionResponse;
import com.tasteradar.domain.order.api.dto.OwnerOrderStatusPatchRequest;
import com.tasteradar.domain.order.api.dto.OwnerRejectRequest;
import com.tasteradar.domain.order.api.dto.StoreOrderStatDto;
import com.tasteradar.domain.order.api.dto.TodayOrderCountResponse;
import com.tasteradar.domain.order.service.OwnerOrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/orders")
public class OwnerOrderController {

	private final OwnerOrderService ownerOrderService;

	@PostMapping("/{orderId}/accept")
	public OrderActionResponse accept(@PathVariable long orderId, Authentication authentication) {
		return ownerOrderService.accept(parseUserId(authentication), orderId);
	}

	@PostMapping("/{orderId}/reject")
	public OrderActionResponse reject(
			@PathVariable long orderId,
			Authentication authentication,
			@Valid @RequestBody OwnerRejectRequest request
	) {
		return ownerOrderService.reject(parseUserId(authentication), orderId, request);
	}

	@PatchMapping("/{orderId}/status")
	public OrderActionResponse patchStatus(
			@PathVariable long orderId,
			Authentication authentication,
			@Valid @RequestBody OwnerOrderStatusPatchRequest request
	) {
		return ownerOrderService.patchStatus(parseUserId(authentication), orderId, request);
	}

	@GetMapping("/stats/today")
	public TodayOrderCountResponse statsToday(Authentication authentication) {
		return ownerOrderService.statsToday(parseUserId(authentication));
	}

	@GetMapping("/stats/today/stores")
	public List<StoreOrderStatDto> statsTodayByStore(Authentication authentication) {
		return ownerOrderService.statsTodayByStore(parseUserId(authentication));
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
