package com.tasteradar.demo.service;

import com.tasteradar.demo.DemoSeedCatalog;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 데모 시드 RESET 시에만 사용. 앱의 소프트 삭제(@SQLDelete)와 달리 FK를 풀기 위해
 * [데모] 가게 관련 행을 DB에서 물리 삭제한다. 운영 API의 삭제 정책은 변경하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class DemoStoreDataCleaner {

	private final EntityManager entityManager;

	@Transactional
	public int hardDeleteAllDemoStores() {
		@SuppressWarnings("unchecked")
		List<Long> storeIds = entityManager
				.createNativeQuery(
						"SELECT id FROM stores WHERE name LIKE :prefix",
						Long.class)
				.setParameter("prefix", DemoSeedCatalog.STORE_NAME_PREFIX + "%")
				.getResultList();

		for (Long storeId : storeIds) {
			hardDeleteStoreData(storeId);
		}
		return storeIds.size();
	}

	private void hardDeleteStoreData(long storeId) {
		exec(
				"""
				DELETE r FROM reviews r
				INNER JOIN orders o ON r.order_id = o.id
				WHERE o.store_id = :storeId
				""",
				storeId);
		exec(
				"""
				DELETE n FROM notifications n
				INNER JOIN orders o ON n.order_id = o.id
				WHERE o.store_id = :storeId
				""",
				storeId);
		exec(
				"""
				DELETE p FROM payment p
				INNER JOIN orders o ON p.order_id = o.id
				WHERE o.store_id = :storeId
				""",
				storeId);
		exec(
				"""
				DELETE oi FROM order_items oi
				INNER JOIN orders o ON oi.order_id = o.id
				WHERE o.store_id = :storeId
				""",
				storeId);
		exec("DELETE FROM orders WHERE store_id = :storeId", storeId);
		exec(
				"""
				DELETE ci FROM cart_items ci
				INNER JOIN cart c ON ci.cart_id = c.id
				WHERE c.store_id = :storeId
				""",
				storeId);
		exec("DELETE FROM cart WHERE store_id = :storeId", storeId);
		exec("DELETE FROM store_images WHERE store_id = :storeId", storeId);
		exec("DELETE FROM menus WHERE store_id = :storeId", storeId);
		exec("DELETE FROM stores WHERE id = :storeId", storeId);
	}

	private void exec(String sql, long storeId) {
		entityManager.createNativeQuery(sql).setParameter("storeId", storeId).executeUpdate();
	}
}
