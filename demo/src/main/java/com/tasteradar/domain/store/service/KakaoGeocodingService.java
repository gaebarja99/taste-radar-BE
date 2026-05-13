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
 * 카카오 로컬 API 로 주소·장소명 → 위도·경도를 조회합니다.
 * - 주소 검색: 도로명/지번 기준 대략 좌표
 * - 키워드 검색: 가게명·POI 기준 보다 정확한 좌표 (가게명이 있으면 우선 시도)
 */
@Service
@RequiredArgsConstructor
public class KakaoGeocodingService {

	private static final String LOCAL_API = "https://dapi.kakao.com";

	private final JsonMapper jsonMapper;
	private final RestClient restClient = RestClient.create();

	@Value("${kakao.rest-api-key:}")
	private String restApiKey;

	public GeocodingResponse geocode(String address) {
		return geocode(address, null);
	}

	public GeocodingResponse geocode(String address, String placeName) {
		String addr = normalize(address);
		String place = normalize(placeName);
		if (addr == null && place == null) {
			return GeocodingResponse.empty("");
		}
		ensureApiKey();

		if (place != null) {
			GeocodingResponse keyword = searchKeyword(place, addr);
			if (keyword.latitude() != null && keyword.longitude() != null) {
				return keyword;
			}
		}
		if (addr != null) {
			return searchAddress(addr);
		}
		return GeocodingResponse.empty(place != null ? place : "");
	}

	private GeocodingResponse searchAddress(String address) {
		String json = restClient.get()
				.uri(LOCAL_API + "/v2/local/search/address.json?query={q}", address)
				.header(HttpHeaders.AUTHORIZATION, authHeader())
				.retrieve()
				.body(String.class);
		if (json == null || json.isBlank()) {
			return GeocodingResponse.empty(address);
		}
		try {
			JsonNode docs = jsonMapper.readTree(json).path("documents");
			if (!docs.isArray() || docs.isEmpty()) {
				return GeocodingResponse.empty(address);
			}
			JsonNode doc = docs.get(0);
			Double lng = readDouble(doc.path("x"));
			Double lat = readDouble(doc.path("y"));
			String roadAddress = readText(doc.path("road_address").path("address_name"));
			String jibunAddress = readText(doc.path("address").path("address_name"));
			return new GeocodingResponse(address, roadAddress, jibunAddress, lat, lng);
		} catch (JacksonException e) {
			throw new IllegalStateException("Invalid Kakao address geocoding JSON", e);
		}
	}

	private GeocodingResponse searchKeyword(String placeName, String addressHint) {
		String json = restClient.get()
				.uri(LOCAL_API + "/v2/local/search/keyword.json?query={q}", placeName)
				.header(HttpHeaders.AUTHORIZATION, authHeader())
				.retrieve()
				.body(String.class);
		if (json == null || json.isBlank()) {
			return GeocodingResponse.empty(placeName);
		}
		try {
			JsonNode docs = jsonMapper.readTree(json).path("documents");
			if (!docs.isArray() || docs.isEmpty()) {
				return GeocodingResponse.empty(placeName);
			}
			JsonNode doc = pickKeywordDoc(docs, addressHint);
			Double lng = readDouble(doc.path("x"));
			Double lat = readDouble(doc.path("y"));
			String roadAddress = readText(doc.path("road_address_name"));
			String jibunAddress = readText(doc.path("address_name"));
			return new GeocodingResponse(placeName, roadAddress, jibunAddress, lat, lng);
		} catch (JacksonException e) {
			throw new IllegalStateException("Invalid Kakao keyword geocoding JSON", e);
		}
	}

	private JsonNode pickKeywordDoc(JsonNode docs, String addressHint) {
		if (addressHint == null) {
			return docs.get(0);
		}
		for (JsonNode doc : docs) {
			String road = readText(doc.path("road_address_name"));
			String jibun = readText(doc.path("address_name"));
			if (addressMatches(road, addressHint) || addressMatches(jibun, addressHint)) {
				return doc;
			}
		}
		return docs.get(0);
	}

	private static boolean addressMatches(String candidate, String hint) {
		if (candidate == null || hint == null) {
			return false;
		}
		String a = candidate.replaceAll("\\s+", "");
		String b = hint.replaceAll("\\s+", "");
		if (a.isEmpty() || b.isEmpty()) {
			return false;
		}
		return a.contains(b) || b.contains(a);
	}

	private void ensureApiKey() {
		if (restApiKey == null || restApiKey.isBlank()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Kakao REST API key is not configured");
		}
	}

	private String authHeader() {
		return "KakaoAK " + restApiKey;
	}

	private static String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
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
