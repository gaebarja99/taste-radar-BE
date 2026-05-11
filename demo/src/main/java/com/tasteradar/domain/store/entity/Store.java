package com.tasteradar.domain.store.entity;

import com.tasteradar.domain.cart.entity.Cart;
import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.user.entity.User;
import com.tasteradar.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stores")
public class Store extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User owner;

	@Column(name = "adress", nullable = false)
	private String address;

	@Column(name = "address_detail", nullable = false)
	private String addressDetail;

	@Column(name = "min_order_amount", nullable = false)
	private int minOrderAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "store_status", nullable = false, length = 32)
	private StoreStatus storeStatus;

	@Column(name = "open_time", nullable = false)
	private LocalTime openTime;

	@Column(name = "close_time", nullable = false)
	private LocalTime closeTime;

	@Column(name = "average_rating", nullable = false)
	private double averageRating;

	@Column(name = "review_count", nullable = false)
	private long reviewCount;

	@Column(name = "required_time", nullable = false)
	private int requiredTimeMinutes;

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoreImage> images = new ArrayList<>();

	@OneToMany(mappedBy = "store")
	private List<Menu> menus = new ArrayList<>();

	@OneToMany(mappedBy = "store")
	private List<Cart> carts = new ArrayList<>();

	@OneToMany(mappedBy = "store")
	private List<FoodOrder> orders = new ArrayList<>();
}
