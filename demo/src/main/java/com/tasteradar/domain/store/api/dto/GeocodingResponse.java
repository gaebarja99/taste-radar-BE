package com.tasteradar.domain.store.api.dto;

/**
 * 주소 → 좌표 변환 결과.
 *
 * @param query        원본 검색어 (사용자 입력)
 * @param roadAddress  도로명 주소 (없을 수 있음)
 * @param jibunAddress 지번 주소 (없을 수 있음)
 * @param latitude     위도 (없을 수 있음 — 매칭 실패 시 null)
 * @param longitude    경도 (없을 수 있음 — 매칭 실패 시 null)
 */
public record GeocodingResponse(
		String query,
		String roadAddress,
		String jibunAddress,
		Double latitude,
		Double longitude
) {
	public static GeocodingResponse empty(String query) {
		return new GeocodingResponse(query, null, null, null, null);
	}
}
