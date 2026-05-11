package com.tasteradar.domain.store.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreStatus {
	PREPARING("준비 중"),
	OPEN("영업 중"),
	CLOSE("영업 종료");

	private final String description;
}
