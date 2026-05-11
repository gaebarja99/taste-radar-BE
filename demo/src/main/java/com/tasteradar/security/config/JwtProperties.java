package com.tasteradar.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
		String issuer,
		String secret,
		long accessTokenValidityMs,
		long refreshTokenValidityMs
) {
}
