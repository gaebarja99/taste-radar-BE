package com.tasteradar.domain.review.service;

import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderStatus;
import com.tasteradar.domain.order.repository.FoodOrderRepository;
import com.tasteradar.domain.review.api.dto.MyReviewResponse;
import com.tasteradar.domain.review.api.dto.OwnerReplyRequest;
import com.tasteradar.domain.review.api.dto.ReviewCreateRequest;
import com.tasteradar.domain.review.api.dto.ReviewTasteDto;
import com.tasteradar.domain.review.api.dto.ReviewUpdateRequest;
import com.tasteradar.domain.review.api.dto.StoreReviewResponse;
import com.tasteradar.domain.review.entity.Review;
import com.tasteradar.domain.review.repository.ReviewRepository;
import com.tasteradar.domain.store.repository.StoreRepository;
import java.util.List;
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
		applyTaste(r, request.taste());
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
		applyTaste(r, request.taste());
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

	private void applyTaste(Review r, ReviewTasteDto t) {
		r.setSweetness(t.sweetness());
		r.setSaltiness(t.saltiness());
		r.setSourness(t.sourness());
		r.setBitterness(t.bitterness());
		r.setUmami(t.umami());
	}

	private MyReviewResponse toMy(Review r) {
		var t = new ReviewTasteDto(r.getSweetness(), r.getSaltiness(), r.getSourness(), r.getBitterness(), r.getUmami());
		return new MyReviewResponse(
				r.getId(),
				r.getOrder().getId(),
				r.getOrder().getStore().getId(),
				r.getOrder().getStore().getName(),
				r.getRating(),
				r.getContent(),
				t,
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
