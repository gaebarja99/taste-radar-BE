package com.tasteradar.domain.review.repository;

import com.tasteradar.domain.review.entity.Review;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByUser_IdOrderByCreatedAtDesc(Long userId);

	Page<Review> findByOrder_Store_IdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

	boolean existsByOrder_Id(Long orderId);

	Optional<Review> findByIdAndUser_Id(Long id, Long userId);

	@Query("select coalesce(avg(r.rating), 0) from Review r join r.order o where o.store.id = :storeId")
	double averageRatingForStore(@Param("storeId") long storeId);

	@Query("select count(r) from Review r join r.order o where o.store.id = :storeId")
	long countReviewsForStore(@Param("storeId") long storeId);

	@Query("select coalesce(avg(r.rating), 0) from Review r join r.order o where o.store.owner.id = :ownerId")
	double averageRatingForOwner(@Param("ownerId") long ownerId);

	@Query("select count(r) from Review r join r.order o where o.store.owner.id = :ownerId")
	long countReviewsForOwner(@Param("ownerId") long ownerId);

	@Query("select count(r) from Review r join r.order o where o.store.owner.id = :ownerId and r.rating = :rating")
	long countByRatingForOwner(@Param("ownerId") long ownerId, @Param("rating") int rating);

	@Query("select count(r) from Review r join r.order o where o.store.owner.id = :ownerId and r.rating <= 2")
	long countLowRatingForOwner(@Param("ownerId") long ownerId);

	@Query("""
			select avg(case when r.sweetness = true then 1.0 else 0.0 end) as avgSweetness,
			       avg(case when r.saltiness = true then 1.0 else 0.0 end) as avgSaltiness,
			       avg(case when r.sourness = true then 1.0 else 0.0 end) as avgSourness,
			       avg(case when r.bitterness = true then 1.0 else 0.0 end) as avgBitterness,
			       avg(case when r.umami = true then 1.0 else 0.0 end) as avgUmami
			from Review r
			join r.order o
			where o.store.id = :storeId
			""")
	Optional<StoreTasteAggregateProjection> aggregateTasteForStore(@Param("storeId") long storeId);

	@Query("""
			select o.store.id as storeId,
			       avg(case when r.sweetness = true then 1.0 else 0.0 end) as avgSweetness,
			       avg(case when r.saltiness = true then 1.0 else 0.0 end) as avgSaltiness,
			       avg(case when r.sourness = true then 1.0 else 0.0 end) as avgSourness,
			       avg(case when r.bitterness = true then 1.0 else 0.0 end) as avgBitterness,
			       avg(case when r.umami = true then 1.0 else 0.0 end) as avgUmami
			from Review r
			join r.order o
			where o.store.id in :storeIds
			group by o.store.id
			""")
	List<StoreTasteBatchProjection> aggregateTasteForStores(@Param("storeIds") Collection<Long> storeIds);
}

