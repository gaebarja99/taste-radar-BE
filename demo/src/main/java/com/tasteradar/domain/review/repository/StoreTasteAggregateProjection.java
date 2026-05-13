package com.tasteradar.domain.review.repository;

public interface StoreTasteAggregateProjection {

	Double getAvgSweetness();

	Double getAvgSaltiness();

	Double getAvgSourness();

	Double getAvgBitterness();

	Double getAvgUmami();
}
