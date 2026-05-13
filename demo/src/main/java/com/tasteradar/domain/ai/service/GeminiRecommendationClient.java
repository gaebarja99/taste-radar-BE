package com.tasteradar.domain.ai.service;

import com.tasteradar.domain.ai.service.dto.LlmMenuRecommendation;
import com.tasteradar.domain.ai.service.dto.MenuRecommendContext;
import com.tasteradar.domain.store.api.dto.StoreTasteProfileResponse;
import com.tasteradar.domain.user.entity.UserTastePreference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class GeminiRecommendationClient {

	private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final RestClient restClient = RestClient.create();

	@Value("${app.ai.gemini-api-key:}")
	private String apiKey;

	@Value("${app.ai.model:gemini-2.5-flash-lite}")
	private String model;

	@Value("${app.ai.fallback-models:gemini-2.5-flash-lite,gemini-2.5-flash}")
	private String fallbackModels;

	@PostConstruct
	void logStartupConfig() {
		if (isConfigured()) {
			log.info("Gemini AI enabled: model={}, fallbacks={}", model.trim(), fallbackModels);
		} else {
			log.warn("Gemini AI disabled: GEMINI_API_KEY is empty — menu recommendations use rule-based fallback only");
		}
	}

	public boolean isConfigured() {
		return apiKey != null && !apiKey.isBlank();
	}

	public Optional<LlmMenuRecommendation> recommend(MenuRecommendContext context) {
		if (!isConfigured()) {
			return Optional.empty();
		}
		Map<String, Object> body = buildRequestBody(context);
		boolean quotaExhausted = false;

		for (String candidateModel : modelsToTry()) {
			try {
				String responseBody = restClient.post()
						.uri(GEMINI_BASE_URL + candidateModel + ":generateContent?key=" + apiKey.trim())
						.contentType(MediaType.APPLICATION_JSON)
						.body(body)
						.retrieve()
						.body(String.class);

				Optional<LlmMenuRecommendation> parsed = parseResponse(responseBody);
				if (parsed.isPresent()) {
					if (!candidateModel.equals(model.trim())) {
						log.info("Gemini recommendation succeeded with fallback model={}", candidateModel);
					}
					return parsed;
				}
				log.warn("Gemini empty/invalid response for model={}", candidateModel);
			} catch (RestClientResponseException e) {
				if (isQuotaExceeded(e)) {
					quotaExhausted = true;
					log.warn("Gemini quota exceeded for model={}, trying next model", candidateModel);
					continue;
				}
				log.warn("Gemini recommendation failed for model={}: {} {}", candidateModel, e.getStatusCode(),
						e.getResponseBodyAsString());
				return Optional.empty();
			} catch (Exception e) {
				log.warn("Gemini recommendation error for model={}: {}", candidateModel, e.getMessage());
				return Optional.empty();
			}
		}

		if (quotaExhausted) {
			log.warn("Gemini free-tier quota exhausted for all configured models; using rule-based fallback");
		}
		return Optional.empty();
	}

	private List<String> modelsToTry() {
		Set<String> ordered = new LinkedHashSet<>();
		addModel(ordered, model);
		if (fallbackModels != null && !fallbackModels.isBlank()) {
			for (String part : fallbackModels.split(",")) {
				addModel(ordered, part);
			}
		}
		return List.copyOf(ordered);
	}

	private void addModel(Set<String> models, String value) {
		if (value == null) {
			return;
		}
		String trimmed = value.trim();
		if (!trimmed.isEmpty()) {
			models.add(trimmed);
		}
	}

	private boolean isQuotaExceeded(RestClientResponseException e) {
		if (e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
			return true;
		}
		String body = e.getResponseBodyAsString();
		return body != null && (body.contains("RESOURCE_EXHAUSTED") || body.contains("quota"));
	}

	private Map<String, Object> buildRequestBody(MenuRecommendContext context) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt()))));
		body.put("contents", List.of(Map.of(
				"role", "user",
				"parts", List.of(Map.of("text", userPrompt(context)))
		)));
		body.put("generationConfig", Map.of(
				"temperature", 0.4,
				"maxOutputTokens", 500,
				"responseMimeType", "application/json"
		));
		return body;
	}

	private Optional<LlmMenuRecommendation> parseResponse(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return Optional.empty();
		}
		try {
			JsonNode root = JSON.readTree(responseBody);
			JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
			if (!parts.isArray() || parts.isEmpty()) {
				log.warn("Gemini response has no candidates/parts");
				return Optional.empty();
			}
			JsonNode textNode = parts.path(0).path("text");
			if (textNode.isMissingNode() || textNode.isNull() || !textNode.isTextual()) {
				log.warn("Gemini response missing text content");
				return Optional.empty();
			}
			String content = textNode.asText().trim();
			if (content.isBlank()) {
				return Optional.empty();
			}
			JsonNode parsed = JSON.readTree(content);
			JsonNode messageNode = parsed.path("message");
			if (messageNode.isMissingNode() || messageNode.isNull() || !messageNode.isTextual()) {
				log.warn("Gemini JSON missing message field: {}", content);
				return Optional.empty();
			}
			String message = messageNode.asText().trim();
			if (message.isBlank()) {
				return Optional.empty();
			}
			List<Long> menuIds = new ArrayList<>();
			JsonNode ids = parsed.path("menuIds");
			if (ids.isArray()) {
				ids.forEach(node -> parseMenuId(node).ifPresent(menuIds::add));
			}
			if (menuIds.isEmpty()) {
				log.warn("Gemini JSON missing menuIds: {}", content);
				return Optional.empty();
			}
			return Optional.of(new LlmMenuRecommendation(message, menuIds));
		} catch (Exception e) {
			log.warn("Gemini response parse error: {}", e.getMessage());
			return Optional.empty();
		}
	}

	private Optional<Long> parseMenuId(JsonNode node) {
		if (node.isNumber()) {
			return Optional.of(node.longValue());
		}
		if (node.isString()) {
			try {
				return Optional.of(Long.parseLong(node.asText().trim()));
			} catch (NumberFormatException ignored) {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	private String systemPrompt() {
		return """
				당신은 한국 음식 배달 앱의 메뉴 추천 어시스턴트입니다.
				사용자 입맛과 가게 리뷰 기반 맛 프로필을 비교해 메뉴 2~3개를 추천합니다.

				반드시 아래 JSON 형식만 출력하세요. 다른 텍스트는 금지합니다.
				{"message":"추천 문구","menuIds":[숫자,...]}

				규칙:
				- message는 1~2문장, 친근한 한국어
				- menuIds는 사용자 프롬프트에 제공된 메뉴 id만 사용 (2~3개)
				- 맛이 잘 맞으면 겹치는 맛을 언급하고, 안 맞으면 솔직히 말한 뒤 무난한 메뉴 추천
				""";
	}

	private String userPrompt(MenuRecommendContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append("가게: ").append(ctx.storeName()).append('\n');
		sb.append("[사용자 선호 입맛]\n").append(formatUserTaste(ctx.userTaste())).append('\n');
		sb.append("[가게 리뷰 맛 프로필 0~5, 리뷰 특화 맛 비율]\n")
				.append(formatStoreProfile(ctx.storeProfile())).append('\n');
		sb.append("[메뉴 목록]\n");
		for (MenuRecommendContext.MenuCandidate menu : ctx.menus()) {
			sb.append("- id:").append(menu.id())
					.append(" 이름:").append(menu.name())
					.append(" 가격:").append(menu.price())
					.append(" 설명:").append(menu.description())
					.append('\n');
		}
		return sb.toString();
	}

	private String formatUserTaste(UserTastePreference taste) {
		if (taste == null) {
			return "설정 없음";
		}
		List<String> labels = new ArrayList<>();
		if (taste.isSweet()) labels.add("단맛");
		if (taste.isSalty()) labels.add("짠맛");
		if (taste.isSour()) labels.add("신맛");
		if (taste.isBitter()) labels.add("쓴맛");
		if (taste.isUmami()) labels.add("감칠맛");
		return labels.isEmpty() ? "설정 없음" : String.join(", ", labels);
	}

	private String formatStoreProfile(StoreTasteProfileResponse profile) {
		if (profile == null) {
			return "리뷰 부족으로 프로필 없음";
		}
		return "단맛 " + profile.sweetness()
				+ ", 짠맛 " + profile.saltiness()
				+ ", 신맛 " + profile.sourness()
				+ ", 쓴맛 " + profile.bitterness()
				+ ", 감칠맛 " + profile.umami()
				+ " (리뷰 " + profile.reviewCount() + "건)";
	}
}
