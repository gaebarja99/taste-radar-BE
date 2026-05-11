package com.tasteradar.domain.store.repository;

import static com.tasteradar.domain.menu.entity.QMenu.menu;
import static com.tasteradar.domain.store.entity.QStore.store;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tasteradar.domain.store.entity.Store;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class StoreSearchRepositoryImpl implements StoreSearchRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Store> searchByStoreNameOrMenuName(String query, Pageable pageable) {
		boolean hasQuery = StringUtils.hasText(query);

		JPAQuery<Store> base = queryFactory
				.selectFrom(store)
				.leftJoin(store.menus, menu)
				.where(
						store.isDeleted.isFalse(),
						hasQuery
								? store.name.containsIgnoreCase(query).or(menu.name.containsIgnoreCase(query))
								: null
				)
				.distinct();

		List<Store> content = base
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		Long total = queryFactory
				.select(store.id.countDistinct())
				.from(store)
				.leftJoin(store.menus, menu)
				.where(
						store.isDeleted.isFalse(),
						hasQuery
								? store.name.containsIgnoreCase(query).or(menu.name.containsIgnoreCase(query))
								: null
				)
				.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}
}

