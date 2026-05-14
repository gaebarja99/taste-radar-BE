package com.tasteradar.domain.user.entity;

import com.tasteradar.domain.cart.entity.Cart;
import com.tasteradar.domain.notification.entity.Notification;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.review.entity.Review;
import com.tasteradar.domain.store.entity.Store;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Email
	@NotBlank
	@Size(max = 200)
	@Column(nullable = false, length = 200, unique = true)
	private String email;

	@NotBlank
	@Size(max = 10)
	@Column(nullable = false, length = 10)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private UserRole role;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	@Column(name = "password_hash", length = 100)
	private String passwordHash;

	@Size(max = 10)
	@Column(name = "zip_code", length = 10)
	private String zipCode;

	@Size(max = 200)
	@Column(length = 200)
	private String address;

	@Size(max = 100)
	@Column(name = "address_detail", length = 100)
	private String addressDetail;

	@Column(name = "kakao_id")
	private Long kakaoId;

	@Size(max = 512)
	@Column(name = "kakao_talk_access_token", length = 512)
	private String kakaoTalkAccessToken;

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private UserTastePreference tastePreference;

	@OneToMany(mappedBy = "owner")
	private List<Store> stores = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<Cart> carts = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<FoodOrder> orders = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<Review> reviews = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<Notification> notifications = new ArrayList<>();
}
