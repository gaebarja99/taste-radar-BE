package com.tasteradar.domain.menu.entity;

import com.tasteradar.domain.store.entity.Store;
import com.tasteradar.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(
		name = "menus",
		indexes = {
				@Index(name = "idx_menus_store_id", columnList = "store_id"),
				@Index(name = "idx_menus_name", columnList = "name"),
				@Index(name = "idx_menus_is_deleted", columnList = "is_deleted")
		}
)
@SQLDelete(sql = "UPDATE menus SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Menu extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private long price;

	@Column(name = "menu_description", nullable = false, columnDefinition = "text")
	private String menuDescription;

	@Column(name = "image_url", nullable = false, columnDefinition = "text")
	private String imageUrl;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;
}
