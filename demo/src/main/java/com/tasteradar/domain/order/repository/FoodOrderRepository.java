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

	@Query("select new com.tasteradar.domain.order.api.dto.StoreOrderStatDto(s.id, s.name, count(o)) from FoodOrder o join o.store s where s.owner.id = :ownerId and o.createdAt >= :start and o.createdAt < :end group by s.id, s.name")
	List<StoreOrderStatDto> countTodayGroupedByStore(@Param("ownerId") long ownerId, @Param("start") Instant start, @Param("end") Instant end);
}

