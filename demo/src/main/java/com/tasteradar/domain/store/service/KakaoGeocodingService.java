package com.tasteradar.domain.store.service;

import com.tasteradar.domain.store.api.dto.GeocodingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 카카오 로컬 API "주소 검색" 으로 도로명/지번 주소와 위도·경도를 조회합니다.
 * - 키는 application.yml `kakao.rest-api-key` 에 설정 (환경변수 KAKAO_REST_API_KEY 권장).
 * - 매칭 실패 시 위도/경도는 null 로 반환.
 */
@Service
@RequiredArgsConstructor
public class KakaoGeocodingService {

	private static final String LOCAL_API = "https://dapi.kakao.com";

	private final JsonMapper jsonMapper;

	@Value("${kakao.rest-api-key:}")
	private String restApiKey;

	public GeocodingResponse geocode(String address) {
		if (address == null || address.isBlank()) {
			return GeocodingResponse.empty(address);
		}
		if (restApiKey == null || restApiKey.isBlank()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Kakao REST API key is not configured");
		}
		String json = RestClient.create()
				.get()
				.uri(LOCAL_API + "/v2/local/search/address.json?query={q}", address)
				.header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
				.retrieve()
				.body(String.class);
		if (json == null || json.isBlank()) {
			return GeocodingResponse.empty(address);
		}
		try {
			JsonNode root = jsonMapper.readTree(json);
			JsonNode docs = root.path("documents");
			if (!docs.isArray() || docs.size() == 0) {
				return GeocodingResponse.empty(address);
			}
			JsonNode doc = docs.get(0);
			Double lng = readDouble(doc.path("x"));
			Double lat = readDouble(doc.path("y"));
			String roadAddress = readText(doc.path("road_address").path("address_name"));
			String jibunAddress = readText(doc.path("address").path("address_name"));
			return new GeocodingResponse(address, roadAddress, jibunAddress, lat, lng);
		} catch (JacksonException e) {
			throw new IllegalStateException("Invalid Kakao geocoding JSON", e);
		}
	}

	private static String readText(JsonNode n) {
		if (n == null || n.isMissingNode() || n.isNull()) return null;
		return n.isTextual() ? n.asText() : null;
	}

	private static Double readDouble(JsonNode n) {
		if (n == null || n.isMissingNode() || n.isNull()) return null;
		if (n.isNumber()) return n.asDouble();
		if (n.isTextual()) {
			try {
				return Double.parseDouble(n.asText());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}
}
