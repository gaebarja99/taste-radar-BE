package com.tasteradar.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 파일 업로드 관련 설정.
 *
 * application.yml 예시:
 * <pre>
 * app:
 *   upload:
 *     dir: ./uploads          # 실제 파일 저장 디렉터리 (절대/상대 경로)
 *     public-base-url: http://localhost:8080/uploads
 * </pre>
 */
@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(
		String dir,
		String publicBaseUrl
) {
	public String resolvedDir() {
		return dir == null || dir.isBlank() ? "./uploads" : dir;
	}

	public String resolvedPublicBaseUrl() {
		return publicBaseUrl == null || publicBaseUrl.isBlank()
				? "http://localhost:8080/uploads"
				: publicBaseUrl;
	}
}
