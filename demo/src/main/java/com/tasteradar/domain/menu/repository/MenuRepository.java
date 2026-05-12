package com.tasteradar.domain.menu.repository;

import com.tasteradar.domain.menu.entity.Menu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {

	List<Menu> findByStoreId(Long storeId);

	Optional<Menu> findByIdAndStore_Id(Long id, Long storeId);
}

