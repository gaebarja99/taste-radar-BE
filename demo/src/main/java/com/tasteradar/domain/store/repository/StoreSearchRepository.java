package com.tasteradar.domain.store.repository;

import com.tasteradar.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreSearchRepository {

	Page<Store> searchByStoreNameOrMenuName(String query, Pageable pageable);
}

