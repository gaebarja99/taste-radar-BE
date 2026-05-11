package com.tasteradar.domain.store.repository;

import com.tasteradar.domain.store.entity.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreSearchRepository {

	Optional<Store> findByIdAndOwner_Id(Long id, Long ownerId);
}
