package com.tasteradar.domain.ai.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class AiCacheService {

	private final StringRedisTemplate redisTemplate;
	private final JsonMapper jsonMapper;

	public <T> T getOrCompute(String key, Duration ttl, Class<T> type, java.util.function.Supplier<T> supplier) {
		String cached = redisTemplate.opsForValue().get(key);
		if (cached != null) {
			try {
				return jsonMapper.readValue(cached, type);
			} catch (JacksonException ignored) {
				// fall through to recompute
			}
		}
		T computed = supplier.get();
		try {
			redisTemplate.opsForValue().set(key, jsonMapper.writeValueAsString(computed), ttl);
		} catch (JacksonException ignored) {
			// if serialization fails, just return computed without caching
		}
		return computed;
	}
}

