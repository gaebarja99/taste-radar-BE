package com.tasteradar.domain.upload.api;

import com.tasteradar.domain.upload.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사장 전용 파일 업로드 엔드포인트.
 * - SecurityConfig 에서 /api/owner/** 는 ROLE_OWNER 만 접근 가능.
 * - 응답으로 받은 URL 을 메뉴 등록 시 imageUrl 로 그대로 넘겨준다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/uploads")
public class OwnerUploadController {

	private final LocalFileStorageService storage;

	@PostMapping("/image")
	public UploadResponse uploadImage(@RequestParam("file") MultipartFile file) {
		LocalFileStorageService.StoredFile stored = storage.storeImage(file);
		return new UploadResponse(stored.url(), stored.filename());
	}

	public record UploadResponse(String url, String filename) {}
}
