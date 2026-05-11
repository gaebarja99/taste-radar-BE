package com.tasteradar.security.provider;

import com.tasteradar.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(long userId, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.accessTokenValidityMs());
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.issuer(jwtProperties.issuer())
				.issuedAt(now)
				.expiration(expiry)
				.claim("role", role)
				.signWith(secretKey, Jwts.SIG.HS256)
				.compact();
	}

	public String createRefreshToken(long userId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.refreshTokenValidityMs());
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.issuer(jwtProperties.issuer())
				.issuedAt(now)
				.expiration(expiry)
				.claim("typ", "refresh")
				.signWith(secretKey, Jwts.SIG.HS256)
				.compact();
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (RuntimeException ex) {
			return false;
		}
	}

	public boolean isRefreshToken(String token) {
		return "refresh".equals(parseClaims(token).get("typ", String.class));
	}

	public long getUserId(String token) {
		return Long.parseLong(parseClaims(token).getSubject());
	}

	public String getRole(String token) {
		return parseClaims(token).get("role", String.class);
	}

	/**
	 * Refresh 토큰에는 role 클레임이 없을 수 있어서, access 재발급 시엔 null을 허용합니다.
	 * (추후 DB에서 role 조회로 바꾸는 것을 권장)
	 */
	public String getRoleOrNull(String token) {
		return parseClaims(token).get("role", String.class);
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
