package com.tasteradar.domain.order.repository;

import com.tasteradar.domain.order.api.dto.StoreOrderStatDto;
import com.tasteradar.domain.order.entity.FoodOrder;
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

	@Query("select count(o) from FoodOrder o where o.store.owner.id = :ownerId and o.createdAt >= :start and o.createdAt < :end")
	long countTodayByOwner(@Param("ownerId") long ownerId, @Param("start") Instant start, @Param("end") Instant end);

	/**
	 * 사장의 가게 전체를 기준으로 오늘 주문 수를 집계합니다.
	 * - 오늘 주문이 0건인 가게도 count=0 으로 함께 반환되도록 Store 기준 LEFT JOIN 사용.
	 * - 삭제된 가게는 제외.
	 */
	@Query("select new com.tasteradar.domain.order.api.dto.StoreOrderStatDto(s.id, s.name, count(o)) " +
			"from com.tasteradar.domain.store.entity.Store s " +
			"left join FoodOrder o on o.store = s and o.createdAt >= :start and o.createdAt < :end " +
			"where s.owner.id = :ownerId and s.isDeleted = false " +
			"group by s.id, s.name " +
			"order by s.id asc")
	List<StoreOrderStatDto> countTodayGroupedByStore(@Param("ownerId") long ownerId, @Param("start") Instant start, @Param("end") Instant end);
}

