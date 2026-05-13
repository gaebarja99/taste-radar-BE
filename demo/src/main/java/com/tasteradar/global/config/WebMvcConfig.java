package com.tasteradar.global.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * /uploads/** 요청을 디스크의 업로드 디렉터리로 매핑.
 * 보안은 SecurityConfig 에서 permitAll(/uploads/**) 로 열어 둔다.
 */
@Configuration
@EnableConfigurationProperties(UploadProperties.class)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final UploadProperties uploadProperties;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploadPath = Paths.get(uploadProperties.resolvedDir()).toAbsolutePath().normalize();
		String location = uploadPath.toUri().toString();
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(location)
				.setCachePeriod(3600);
	}
}
