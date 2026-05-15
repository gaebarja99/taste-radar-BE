package com.tasteradar.domain.order.repository;

import com.tasteradar.domain.order.api.dto.StoreOrderStatDto;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {

	Page<FoodOrder> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	Optional<FoodOrder> findByIdAndUser_Id(Long id, Long userId);

	/** 사장 권한으로 단건 조회: 해당 주문 가게의 사장이 본인인 경우에만 반환 */
	Optional<FoodOrder> findByIdAndStore_Owner_Id(Long id, Long ownerId);

	@Query("select count(o) from FoodOrder o where o.store.owner.id = :ownerId and o.createdAt >= :start and o.createdAt < :end")
	long countTodayByOwner(@Param("ownerId") long ownerId, @Param("start") Instant start, @Param("end") Instant end);

	@Query(
			value = """
					SELECT COUNT(o.id)
					FROM orders o
					INNER JOIN stores s ON o.store_id = s.id
					WHERE s.user_id = :ownerId
					  AND o.created_at >= :start
					  AND o.created_at < :end
					""",
			nativeQuery = true
	)
	long countTodayByOwnerNative(
			@Param("ownerId") long ownerId,
			@Param("start") Instant start,
			@Param("end") Instant end
	);

	/**
	 * 사장의 주문 목록 조회. storeId, status 는 null 이면 필터링하지 않음.
	 */
	@Query("select o from FoodOrder o " +
			"where o.store.owner.id = :ownerId " +
			"  and (:storeId is null or o.store.id = :storeId) " +
			"  and (:status  is null or o.orderStatus = :status) " +
			"order by o.createdAt desc")
	Page<FoodOrder> findOwnerOrders(
			@Param("ownerId") long ownerId,
			@Param("storeId") Long storeId,
			@Param("status") OrderStatus status,
			Pageable pageable
	);

	/**
	 * 사장의 가게 전체를 기준으로 오늘 주문 수를 집계합니다.
	 * - 오늘 주문이 0건인 가게도 count=0 으로 함께 반환되도록 상관 서브쿼리 사용.
	 *   (Hibernate 6 환경에서 ad-hoc LEFT JOIN 처리 차이를 회피)
	 * - 삭제된 가게는 제외.
	 */
	@Query("select new com.tasteradar.domain.order.api.dto.StoreOrderStatDto(" +
			"   s.id, s.name, " +
			"   (select count(o) from FoodOrder o " +
			"      where o.store = s and o.createdAt >= :start and o.createdAt < :end)" +
			") " +
			"from com.tasteradar.domain.store.entity.Store s " +
			"where s.owner.id = :ownerId and s.isDeleted = false " +
			"order by s.id asc")
	List<StoreOrderStatDto> countTodayGroupedByStore(@Param("ownerId") long ownerId, @Param("start") Instant start, @Param("end") Instant end);

	/**
	 * 가게별 오늘 주문 수 (stores.user_id 기준 — 주간 매출 집계와 동일).
	 * nativeQuery 결과: [0]=storeId, [1]=storeName, [2]=orderCount
	 */
	@Query(
			value = """
					SELECT s.id,
					       s.name,
					       COUNT(o.id)
					FROM stores s
					LEFT JOIN orders o ON o.store_id = s.id
					  AND o.created_at >= :start
					  AND o.created_at < :end
					WHERE s.user_id = :ownerId
					  AND s.is_deleted = false
					GROUP BY s.id, s.name
					ORDER BY s.id ASC
					""",
			nativeQuery = true
	)
	List<Object[]> countTodayGroupedByStoreNative(
			@Param("ownerId") long ownerId,
			@Param("start") Instant start,
			@Param("end") Instant end
	);

	/**
	 * 사장 소유 가게의 배달 완료 주문 매출을 일별로 집계 (Asia/Seoul 기준 DATE).
	 * nativeQuery 결과: [0]=java.sql.Date, [1]=BigDecimal/long salesAmount, [2]=long orderCount
	 */
	@Query(
			value = """
					SELECT DATE(o.created_at) AS order_date,
					       COALESCE(SUM(o.total_amount), 0) AS sales_amount,
					       COUNT(o.id) AS order_count
					FROM orders o
					INNER JOIN stores s ON o.store_id = s.id
					WHERE s.user_id = :ownerId
					  AND o.order_status = 'DELIVERED'
					  AND o.created_at >= :start
					  AND o.created_at < :end
					GROUP BY DATE(o.created_at)
					ORDER BY order_date ASC
					""",
			nativeQuery = true
	)
	List<Object[]> sumDailySalesByOwner(
			@Param("ownerId") long ownerId,
			@Param("start") Instant start,
			@Param("end") Instant end
	);

	@Query(
			value = """
					SELECT COALESCE(SUM(o.total_amount), 0)
					FROM orders o
					INNER JOIN stores s ON o.store_id = s.id
					WHERE s.user_id = :ownerId
					  AND o.order_status = 'DELIVERED'
					  AND o.created_at >= :start
					  AND o.created_at < :end
					""",
			nativeQuery = true
	)
	long sumSalesByOwner(
			@Param("ownerId") long ownerId,
			@Param("start") Instant start,
			@Param("end") Instant end
	);
}

