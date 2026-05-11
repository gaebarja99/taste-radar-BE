package com.tasteradar.domain.user.entity;

import com.tasteradar.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_taste_preferences")
public class UserTastePreference extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(name = "is_sweet", nullable = false)
	private boolean sweet;

	@Column(name = "is_salty", nullable = false)
	private boolean salty;

	@Column(name = "is_sour", nullable = false)
	private boolean sour;

	@Column(name = "is_bitter", nullable = false)
	private boolean bitter;

	@Column(name = "is_umami", nullable = false)
	private boolean umami;
}
