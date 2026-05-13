package com.tasteradar.domain.store.api;

import com.tasteradar.domain.store.api.dto.GeocodingResponse;
import com.tasteradar.domain.store.service.KakaoGeocodingService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사장 전용 주소 → 좌표 변환 프록시.
 * - /api/owner/** 는 SecurityConfig 에서 ROLE_OWNER 로 보호되므로 사장만 호출 가능.
 * - 카카오 REST API 키는 서버에만 보관됨.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/geocode")
public class OwnerGeocodeController {

	private final KakaoGeocodingService kakaoGeocodingService;

	@GetMapping
	public GeocodingResponse geocode(@RequestParam @NotBlank String address) {
		return kakaoGeocodingService.geocode(address);
	}
}
