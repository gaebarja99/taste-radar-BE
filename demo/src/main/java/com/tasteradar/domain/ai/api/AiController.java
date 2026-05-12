package com.tasteradar.domain.ai.api;

import com.tasteradar.domain.ai.api.dto.StoreRecommendationsResponse;
import com.tasteradar.domain.ai.api.dto.TastePentagonResponse;
import com.tasteradar.domain.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

	private final AiService aiService;

	/** 내 리뷰 기반 오각형 맛 통계 (테이블 저장 없이 계산, Redis TTL 캐시) */
	@GetMapping("/me/taste-pentagon")
	public TastePentagonResponse tastePentagon(Authentication authentication) {
		long userId = parseUserId(authentication);
		return aiService.tastePentagon(userId);
	}

	/** 내 선호 입맛 기반 메뉴 추천 메시지 (테이블 저장 없이 계산, Redis TTL 캐시) */
	@GetMapping({"/stores/{storeId}/recommendations", "/stores/{storeId}/recomm"})
	public StoreRecommendationsResponse recommendations(@PathVariable long storeId, Authentication authentication) {
		long userId = parseUserId(authentication);
		return aiService.recommendMenus(userId, storeId);
	}

	private long parseUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(principal));
	}
}

