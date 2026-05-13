package com.tasteradar.domain.review.repository;

public interface StoreTasteBatchProjection {

	Long getStoreId();

	Double getAvgSweetness();

	Double getAvgSaltiness();

	Double getAvgSourness();

	Double getAvgBitterness();

	Double getAvgUmami();
}
