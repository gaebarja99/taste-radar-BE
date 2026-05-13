package com.tasteradar.domain.upload.service;

import com.tasteradar.global.config.UploadProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * 로컬 디스크에 이미지를 저장하고 외부에서 접근 가능한 URL 을 반환한다.
 * - 실제 파일: {app.upload.dir}/{uuid}.{ext}
 * - URL:       {app.upload.public-base-url}/{uuid}.{ext}
 */
@Service
@RequiredArgsConstructor
public class LocalFileStorageService {

	private static final long MAX_BYTES = 5L * 1024 * 1024; // 5MB
	private static final Set<String> ALLOWED_TYPES = Set.of(
			"image/jpeg", "image/png", "image/webp", "image/gif"
	);
	private static final Set<String> ALLOWED_EXTS = Set.of(
			"jpg", "jpeg", "png", "webp", "gif"
	);

	private final UploadProperties properties;

	public StoredFile storeImage(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "5MB 이하의 파일만 업로드할 수 있습니다.");
		}
		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 이미지 형식입니다.");
		}
		String ext = extractExtension(file.getOriginalFilename());
		if (ext == null || !ALLOWED_EXTS.contains(ext)) {
			ext = switch (contentType.toLowerCase(Locale.ROOT)) {
				case "image/jpeg" -> "jpg";
				case "image/png" -> "png";
				case "image/webp" -> "webp";
				case "image/gif" -> "gif";
				default -> "bin";
			};
		}

		try {
			Path dir = Paths.get(properties.resolvedDir()).toAbsolutePath().normalize();
			Files.createDirectories(dir);

			String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
			Path target = dir.resolve(filename).normalize();
			if (!target.startsWith(dir)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 파일 경로입니다.");
			}
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

			String base = properties.resolvedPublicBaseUrl().replaceAll("/+$", "");
			return new StoredFile(filename, base + "/" + filename);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다.", e);
		}
	}

	private String extractExtension(String filename) {
		if (filename == null) return null;
		int dot = filename.lastIndexOf('.');
		if (dot < 0 || dot == filename.length() - 1) return null;
		return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	public record StoredFile(String filename, String url) {}
}
