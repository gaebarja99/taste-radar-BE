package com.tasteradar.security.config;

import com.tasteradar.oauth.kakao.KakaoOAuth2LoginSuccessHandler;
import com.tasteradar.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final KakaoOAuth2LoginSuccessHandler kakaoOAuth2LoginSuccessHandler;
	private final Environment environment;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		boolean test = environment.acceptsProfiles(Profiles.of("test"));
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		if (test) {
			http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		} else {
			RequestMatcher apiMatcher = request -> request.getRequestURI().startsWith("/api/");
			http
					.oauth2Login(oauth2 -> oauth2
							.successHandler(kakaoOAuth2LoginSuccessHandler))
					.exceptionHandling(eh -> eh
							// /api/** 의 미인증 요청은 OAuth 리다이렉트 대신 401 JSON 응답
							.defaultAuthenticationEntryPointFor(
									new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
									apiMatcher)
							// /api/** 의 권한 부족은 403 응답
							.accessDeniedHandler((request, response, ex) -> {
								if (request.getRequestURI().startsWith("/api/")) {
									response.setStatus(HttpServletResponse.SC_FORBIDDEN);
								} else {
									response.sendError(HttpServletResponse.SC_FORBIDDEN);
								}
							}))
					.authorizeHttpRequests(auth -> auth
							.requestMatchers("/oauth2/**", "/login/oauth2/**", "/error",
									"/api/auth/refresh", "/api/auth/kakao/**").permitAll()
							.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
							.requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
							.requestMatchers("/api/owner/**").hasRole("OWNER")
							.requestMatchers("/api/cart/**").hasRole("CUSTOMER")
							.requestMatchers(HttpMethod.PUT, "/api/users/me/tastes").hasRole("CUSTOMER")
							.requestMatchers(HttpMethod.GET, "/api/orders/{orderId:[0-9]+}").hasAnyRole("CUSTOMER", "OWNER")
							.requestMatchers("/api/orders/**").hasRole("CUSTOMER")
							.requestMatchers(HttpMethod.GET, "/api/reviews/me").hasRole("CUSTOMER")
							.requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasRole("CUSTOMER")
							.requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasRole("CUSTOMER")
							.requestMatchers(HttpMethod.POST, "/api/payments/kakaopay/ready", "/api/payments/kakaopay/approve")
							.hasRole("CUSTOMER")
							.requestMatchers(HttpMethod.POST, "/api/payments/kakaopay/cancel").authenticated()
							.requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
							.requestMatchers("/api/users/**", "/api/notifications/**", "/api/ai/**")
							.authenticated()
							.anyRequest().denyAll())
					.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		}
		return http.build();
	}

	/**
	 * 프론트엔드(개발) origin 허용 설정.
	 * - Vite dev: 5173 / 5174
	 * - VS Code Live Server: 5500
	 * 운영 도메인이 정해지면 여기 추가하세요.
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(List.of(
				"http://localhost:5173",
				"http://localhost:5174",
				"http://localhost:5500",
				"http://127.0.0.1:5173",
				"http://127.0.0.1:5174",
				"http://127.0.0.1:5500"
		));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Authorization", "Location"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
