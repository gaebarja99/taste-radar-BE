package com.tasteradar.domain.store.repository;

import com.tasteradar.domain.store.entity.Store;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreSearchRepository {

	Optional<Store> findByIdAndOwner_Id(Long id, Long ownerId);

	/**
	 * 본인이 소유한 모든 가게(소프트 삭제 포함). {@code Store} 엔티티에
	 * {@code @SQLRestriction} 이 없기 때문에 폐업 처리(is_deleted=true)된 가게도 함께 반환된다.
	 */
	java.util.List<Store> findByOwner_IdOrderByIdAsc(Long ownerId);

	/**
	 * Haversine 공식(지구 반지름 6371km) 기반 반경 검색.
	 * - 좌표가 NULL인 가게 / 삭제된 가게는 제외
	 * - 결과는 거리 오름차순
	 */
	@Query(
			value = """
					SELECT s.* FROM stores s
					WHERE s.is_deleted = false
					  AND s.latitude  IS NOT NULL
					  AND s.longitude IS NOT NULL
					  AND (6371 * acos(
					        cos(radians(:lat)) * cos(radians(s.latitude)) *
					        cos(radians(s.longitude) - radians(:lng)) +
					        sin(radians(:lat)) * sin(radians(s.latitude))
					      )) <= :radius
					ORDER BY (6371 * acos(
					        cos(radians(:lat)) * cos(radians(s.latitude)) *
					        cos(radians(s.longitude) - radians(:lng)) +
					        sin(radians(:lat)) * sin(radians(s.latitude))
					      )) ASC
					""",
			countQuery = """
					SELECT COUNT(*) FROM stores s
					WHERE s.is_deleted = false
					  AND s.latitude  IS NOT NULL
					  AND s.longitude IS NOT NULL
					  AND (6371 * acos(
					        cos(radians(:lat)) * cos(radians(s.latitude)) *
					        cos(radians(s.longitude) - radians(:lng)) +
					        sin(radians(:lat)) * sin(radians(s.latitude))
					      )) <= :radius
					""",
			nativeQuery = true
	)
	Page<Store> findNearby(
			@Param("lat") double lat,
			@Param("lng") double lng,
			@Param("radius") double radiusKm,
			Pageable pageable
	);
}
