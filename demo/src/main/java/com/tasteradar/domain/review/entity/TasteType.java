package com.tasteradar.domain.review.entity;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum TasteType {
	SWEET,
	SALTY,
	SOUR,
	BITTER,
	UMAMI;

	public static TasteType fromApiKey(String key) {
		if (key == null || key.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "맛을 선택해 주세요.");
		}
		return switch (key.trim().toLowerCase()) {
			case "sweet" -> SWEET;
			case "salty" -> SALTY;
			case "sour" -> SOUR;
			case "bitter" -> BITTER;
			case "umami" -> UMAMI;
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 맛 값입니다: " + key);
		};
	}

	public String toApiKey() {
		return switch (this) {
			case SWEET -> "sweet";
			case SALTY -> "salty";
			case SOUR -> "sour";
			case BITTER -> "bitter";
			case UMAMI -> "umami";
		};
	}
}
