package com.tasteradar.global.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(this::toFieldError)
				.collect(Collectors.toList());
		String message = errors.isEmpty()
				? "입력값을 확인해 주세요."
				: errors.stream()
						.map(e -> fieldLabel(e.get("field")) + ": " + e.get("message"))
						.collect(Collectors.joining(" · "));
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("message", message);
		body.put("errors", errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("message", ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString());
		return ResponseEntity.status(ex.getStatusCode()).body(body);
	}

	private Map<String, String> toFieldError(FieldError fe) {
		Map<String, String> item = new LinkedHashMap<>();
		item.put("field", fe.getField());
		item.put("message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않습니다.");
		return item;
	}

	private String fieldLabel(String field) {
		return switch (field) {
			case "name" -> "메뉴명";
			case "price" -> "가격";
			case "menuDescription" -> "설명";
			case "imageUrl" -> "이미지 주소";
			default -> field;
		};
	}
}
