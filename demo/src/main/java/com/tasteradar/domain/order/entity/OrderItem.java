package com.tasteradar.domain.order.entity;

import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private FoodOrder order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_id", nullable = false)
	@NotFound(action = NotFoundAction.IGNORE)
	private Menu menu;

	/** soft-delete 등으로 menu 연관이 없을 때 FK 조회용 */
	@Column(name = "menu_id", insertable = false, updatable = false)
	private Long menuId;

	/** 주문 시점 메뉴명 스냅샷 (삭제 후에도 표시) */
	@Column(name = "menu_name", length = 100)
	private String menuName;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private int price;
}
