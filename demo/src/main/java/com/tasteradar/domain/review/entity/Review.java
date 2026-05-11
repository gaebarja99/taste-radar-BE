package com.tasteradar.domain.review.entity;

import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.user.entity.User;
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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reviews")
@SQLDelete(sql = "UPDATE reviews SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Review extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private FoodOrder order;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private int rating;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	@Column(name = "owner_reply", columnDefinition = "text")
	private String ownerReply;

	@Column(nullable = false)
	private int sweetness;

	@Column(nullable = false)
	private int saltiness;

	@Column(nullable = false)
	private int sourness;

	@Column(nullable = false)
	private int bitterness;

	@Column(nullable = false)
	private int umami;
}
