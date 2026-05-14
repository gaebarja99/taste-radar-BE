package com.tasteradar.domain.review.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMenuTasteEntry {

	private Long menuId;
	private String menuName;
	private String taste;
}
