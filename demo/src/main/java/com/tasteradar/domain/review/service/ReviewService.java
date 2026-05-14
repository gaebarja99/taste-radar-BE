package com.tasteradar.domain.review.service;

import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderStatus;
import com.tasteradar.domain.order.repository.FoodOrderRepository;
import com.tasteradar.domain.review.api.dto.MyReviewResponse;
import com.tasteradar.domain.review.api.dto.OwnerReplyRequest;
import com.tasteradar.domain.review.api.dto.ReviewCreateRequest;
import com.tasteradar.domain.review.api.dto.ReviewMenuTasteItemDto;
import com.tasteradar.domain.review.api.dto.ReviewMenuTasteResponse;
import com.tasteradar.domain.review.api.dto.ReviewTasteDto;
import com.tasteradar.domain.review.api.dto.ReviewUpdateRequest;
import com.tasteradar.domain.review.api.dto.StoreReviewResponse;
import com.tasteradar.domain.review.entity.Review;
import com.tasteradar.domain.review.entity.ReviewMenuTasteEntry;
import com.tasteradar.domain.review.entity.TasteType;
import com.tasteradar.domain.review.repository.ReviewRepository;
import com.tasteradar.domain.store.repository.StoreRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final FoodOrderRepository foodOrderRepository;
	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;

	@Transactional
	public MyReviewResponse createForOrder(long userId, long orderId, ReviewCreateRequest request) {
		FoodOrder order = foodOrderRepository.findByIdAndUser_Id(orderId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		if (order.getOrderStatus() != OrderStatus.DELIVERED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order must be DELIVERED to review");
		}
		if (reviewRepository.existsByOrder_Id(orderId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists");
		}
		var user = order.getUser();
		Review r = new Review();
		r.setOrder(order);
		r.setUser(user);
		r.setRating(request.rating());
		r.setContent(request.content());
		r.setDeleted(false);
		applyMenuTastes(r, order, request.menuTastes());
		reviewRepository.save(r);
		refreshStoreReviewStats(order.getStore().getId());
		return toMy(r);
	}

	@Transactional(readOnly = true)
	public Page<StoreReviewResponse> pageForStore(long storeId, Pageable pageable) {
		var store = storeRepository.findById(storeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		if (store.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		return reviewRepository.findByOrder_Store_IdOrderByCreatedAtDesc(storeId, pageable)
				.map(r -> new StoreReviewResponse(
						r.getId(),
						r.getRating(),
						r.getContent(),
						r.getOwnerReply(),
						r.getCreatedAt()
				));
	}

	@Transactional(readOnly = true)
	public List<MyReviewResponse> myReviews(long userId) {
		return reviewRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
				.map(this::toMy)
				.toList();
	}

	@Transactional
	public MyReviewResponse updateMine(long userId, long reviewId, ReviewUpdateRequest request) {
		Review r = reviewRepository.findByIdAndUser_Id(reviewId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
		r.setRating(request.rating());
		r.setContent(request.content());
		applyMenuTastes(r, r.getOrder(), request.menuTastes());
		refreshStoreReviewStats(r.getOrder().getStore().getId());
		return toMy(r);
	}

	@Transactional
	public void deleteMine(long userId, long reviewId) {
		Review r = reviewRepository.findByIdAndUser_Id(reviewId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
		long storeId = r.getOrder().getStore().getId();
		reviewRepository.delete(r);
		refreshStoreReviewStats(storeId);
	}

	@Transactional
	public void ownerReply(long ownerId, long reviewId, OwnerReplyRequest request) {
		Review r = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
		if (r.getOrder().getStore().getOwner().getId() != ownerId) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store review");
		}
		r.setOwnerReply(request.ownerReply());
	}

	@Transactional
	public void deleteByOwner(long ownerId, long reviewId) {
		Review r = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
		if (r.getOrder().getStore().getOwner().getId() != ownerId) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store review");
		}
		long storeId = r.getOrder().getStore().getId();
		reviewRepository.delete(r);
		refreshStoreReviewStats(storeId);
	}

	private void applyMenuTastes(Review review, FoodOrder order, List<ReviewMenuTasteItemDto> menuTastes) {
		Set<Long> expectedMenuIds = order.getItems().stream()
				.map(item -> item.getMenu().getId())
				.collect(Collectors.toSet());
		if (expectedMenuIds.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문에 메뉴가 없어 리뷰를 작성할 수 없습니다.");
		}

		Map<Long, TasteType> tasteByMenuId = new HashMap<>();
		for (ReviewMenuTasteItemDto item : menuTastes) {
			if (item.menuId() == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "메뉴 ID가 필요합니다.");
			}
			if (tasteByMenuId.putIfAbsent(item.menuId(), TasteType.fromApiKey(item.taste())) != null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "메뉴별 맛은 한 가지만 선택할 수 있습니다.");
			}
		}

		if (!tasteByMenuId.keySet().equals(expectedMenuIds)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문한 모든 메뉴에 대해 맛을 하나씩 선택해 주세요.");
		}

		long storeId = order.getStore().getId();
		List<ReviewMenuTasteEntry> entries = new ArrayList<>();
		for (Long menuId : expectedMenuIds) {
			Menu menu = menuRepository.findByIdAndStore_Id(menuId, storeId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문에 포함되지 않은 메뉴입니다."));
			TasteType tasteType = tasteByMenuId.get(menuId);
			entries.add(new ReviewMenuTasteEntry(menuId, menu.getName(), tasteType.toApiKey()));
		}
		review.setMenuTastes(entries);
		applyDerivedTaste(review, new HashSet<>(tasteByMenuId.values()));
	}

	private void applyDerivedTaste(Review review, Set<TasteType> tastes) {
		review.setSweetness(tastes.contains(TasteType.SWEET));
		review.setSaltiness(tastes.contains(TasteType.SALTY));
		review.setSourness(tastes.contains(TasteType.SOUR));
		review.setBitterness(tastes.contains(TasteType.BITTER));
		review.setUmami(tastes.contains(TasteType.UMAMI));
	}

	private MyReviewResponse toMy(Review r) {
		var t = new ReviewTasteDto(
				r.isSweetness(),
				r.isSaltiness(),
				r.isSourness(),
				r.isBitterness(),
				r.isUmami()
		);
		List<ReviewMenuTasteResponse> menuTastes = (r.getMenuTastes() == null ? List.<ReviewMenuTasteEntry>of() : r.getMenuTastes())
				.stream()
				.map(entry -> new ReviewMenuTasteResponse(
						entry.getMenuId(),
						entry.getMenuName(),
						entry.getTaste()
				))
				.toList();
		return new MyReviewResponse(
				r.getId(),
				r.getOrder().getId(),
				r.getOrder().getStore().getId(),
				r.getOrder().getStore().getName(),
				r.getRating(),
				r.getContent(),
				t,
				menuTastes,
				r.getOwnerReply(),
				r.getCreatedAt()
		);
	}

	private void refreshStoreReviewStats(long storeId) {
		var store = storeRepository.findById(storeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
		store.setReviewCount(reviewRepository.countReviewsForStore(storeId));
		store.setAverageRating(reviewRepository.averageRatingForStore(storeId));
	}
}
