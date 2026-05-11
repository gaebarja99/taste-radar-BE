package com.tasteradar.domain.payment.entity;

import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.global.entity.BaseCreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment")
public class Payment extends BaseCreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private FoodOrder order;

	@Column(nullable = false)
	private String tid;

	@Column(nullable = false, length = 64)
	private String cid = "TC0ONETIME";

	@Column(name = "payment_method")
	private String paymentMethod;

	@Column(name = "total_price", nullable = false)
	private int totalPrice;

	@Column(nullable = false)
	private String status;

	@Column(name = "approved_at")
	private Instant approvedAt;

	@Column(name = "canceled_at")
	private Instant canceledAt;
}
