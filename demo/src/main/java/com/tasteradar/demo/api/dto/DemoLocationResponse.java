package com.tasteradar.demo.api.dto;

/**
 * 프론트에서 {@code tasteRadar.nearbySession} 등에 넣을 데모 위치 프리셋.
 */
public record DemoLocationResponse(
		String id,
		String label,
		double lat,
		double lng,
		double radiusKm,
		boolean recommended
) {
}
