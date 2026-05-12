package com.tasteradar.domain.store.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 사장: 가게 정보 수정 요청.
 * - 이미지(images)는 선택. 값이 들어오면 기존 이미지 컬렉션을 통째로 교체합니다.
 * - 상태(storeStatus), 평점, 리뷰 수 등 시스템 관리 필드는 별도 엔드포인트로만 변경.
 */
public record OwnerStoreUpdateRequest(
		@NotBlank String name,
		@NotBlank String address,
		@NotBlank String addressDetail,
		@NotNull @Positive Integer minOrderAmount,
		@NotBlank String openTime,
		@NotBlank String closeTime,
		@NotNull @Positive Integer requiredTimeMinutes,
		Double latitude,
		Double longitude,
		@Valid List<OwnerStoreCreateRequest.OwnerStoreImageRequest> images
) {
}
