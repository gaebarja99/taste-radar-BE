package com.tasteradar.domain.order.entity;

import com.tasteradar.domain.notification.entity.Notification;
import com.tasteradar.domain.payment.entity.Payment;
import com.tasteradar.domain.review.entity.Review;
import com.tasteradar.domain.store.entity.Store;
import com.tasteradar.domain.user.entity.User;
import com.tasteradar.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class FoodOrder extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(name = "order_number", nullable = false, unique = true)
	private String orderNumber;

	@Column(name = "zip_code", nullable = false)
	private String zipCode;

	@Column(nullable = false)
	private String address;

	@Column(name = "address_detail", nullable = false)
	private String addressDetail;

	@Column(name = "order_status", nullable = false)
	private String orderStatus;

	@Column(name = "rejection_reason")
	private String rejectionReason;

	@Column(name = "total_amount", nullable = false)
	private int totalAmount;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	@OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
	private Payment payment;

	@OneToMany(mappedBy = "order")
	private List<Review> reviews = new ArrayList<>();

	@OneToMany(mappedBy = "order")
	private List<Notification> notifications = new ArrayList<>();
}
