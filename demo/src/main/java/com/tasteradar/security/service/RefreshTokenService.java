package com.tasteradar.security.service;

import com.tasteradar.security.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private static final String KEY_PREFIX = "auth:refresh:user:";

	private final StringRedisTemplate stringRedisTemplate;
	private final JwtProperties jwtProperties;

	public void save(long userId, String refreshToken) {
		String key = key(userId);
		String value = hash(refreshToken);
		Duration ttl = Duration.ofMillis(jwtProperties.refreshTokenValidityMs());
		stringRedisTemplate.opsForValue().set(key, value, ttl);
	}

	public boolean matches(long userId, String refreshToken) {
		String stored = stringRedisTemplate.opsForValue().get(key(userId));
		if (stored == null) {
			return false;
		}
		return stored.equals(hash(refreshToken));
	}

	public void rotate(long userId, String newRefreshToken) {
		save(userId, newRefreshToken);
	}

	public void delete(long userId) {
		stringRedisTemplate.delete(key(userId));
	}

	private String key(long userId) {
		return KEY_PREFIX + userId;
	}

	private String hash(String raw) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}

