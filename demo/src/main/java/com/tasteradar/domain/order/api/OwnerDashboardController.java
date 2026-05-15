package com.tasteradar.domain.order.api;

import com.tasteradar.domain.order.api.dto.OwnerRatingSummaryResponse;
import com.tasteradar.domain.order.api.dto.WeeklySalesResponse;
import com.tasteradar.domain.order.service.OwnerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/dashboard")
public class OwnerDashboardController {

	private final OwnerDashboardService ownerDashboardService;

	@GetMapping("/weekly-sales")
	public WeeklySalesResponse weeklySales(Authentication authentication) {
		return ownerDashboardService.weeklySales(parseUserId(authentication));
	}

	@GetMapping("/rating-summary")
	public OwnerRatingSummaryResponse ratingSummary(Authentication authentication) {
		return ownerDashboardService.ratingSummary(parseUserId(authentication));
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}
