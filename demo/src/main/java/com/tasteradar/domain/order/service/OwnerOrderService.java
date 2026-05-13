package com.tasteradar.domain.order.service;

import com.tasteradar.domain.order.api.dto.OrderActionResponse;
import com.tasteradar.domain.order.api.dto.OrderSummaryResponse;
import com.tasteradar.domain.order.api.dto.OwnerOrderStatusPatchRequest;
import com.tasteradar.domain.order.api.dto.OwnerRejectRequest;
import com.tasteradar.domain.order.api.dto.StoreOrderStatDto;
import com.tasteradar.domain.order.api.dto.TodayOrderCountResponse;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderStatus;
import com.tasteradar.domain.order.repository.FoodOrderRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
public class OwnerOrderService {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private final FoodOrderRepository foodOrderRepository;

	@Transactional
	public OrderActionResponse accept(long ownerId, long orderId) {
		FoodOrder order = loadForOwner(ownerId, orderId);
		if (order.getOrderStatus() != OrderStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING can be accepted");
		}
		order.setOrderStatus(OrderStatus.COOKING);
		return new OrderActionResponse(order.getId(), order.getOrderStatus());
	}

	@Transactional
	public OrderActionResponse reject(long ownerId, long orderId, OwnerRejectRequest request) {
		FoodOrder order = loadForOwner(ownerId, orderId);
		if (order.getOrderStatus() != OrderStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING can be rejected");
		}
		order.setOrderStatus(OrderStatus.REJECTED);
		order.setRejectionReason(request.rejectionReason());
		return new OrderActionResponse(order.getId(), order.getOrderStatus());
	}

	@Transactional
	public OrderActionResponse patchStatus(long ownerId, long orderId, OwnerOrderStatusPatchRequest request) {
		FoodOrder order = loadForOwner(ownerId, orderId);
		OrderStatus current = order.getOrderStatus();
		OrderStatus next = request.status();
		if (current == OrderStatus.COOKING && next == OrderStatus.DELIVERING) {
			order.setOrderStatus(OrderStatus.DELIVERING);
		} else if (current == OrderStatus.DELIVERING && next == OrderStatus.DELIVERED) {
			order.setOrderStatus(OrderStatus.DELIVERED);
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid status transition");
		}
		return new OrderActionResponse(order.getId(), order.getOrderStatus());
	}

	@Transactional(readOnly = true)
	public TodayOrderCountResponse statsToday(long ownerId) {
		InstantRange r = todaySeoulRange();
		return new TodayOrderCountResponse(foodOrderRepository.countTodayByOwner(ownerId, r.start(), r.end()));
	}

	@Transactional(readOnly = true)
	public List<StoreOrderStatDto> statsTodayByStore(long ownerId) {
		InstantRange r = todaySeoulRange();
		return foodOrderRepository.countTodayGroupedByStore(ownerId, r.start(), r.end());
	}

	/**
	 * 사장 본인 가게의 주문 목록.
	 * @param storeId null 이면 전체 가게
	 * @param status  null 이면 전체 상태
	 */
	@Transactional(readOnly = true)
	public Page<OrderSummaryResponse> listOrders(long ownerId, Long storeId, OrderStatus status, Pageable pageable) {
		return foodOrderRepository.findOwnerOrders(ownerId, storeId, status, pageable)
				.map(OrderSummaryMapper::toSummary);
	}

	private FoodOrder loadForOwner(long ownerId, long orderId) {
		FoodOrder order = foodOrderRepository.findById(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		if (order.getStore().getOwner().getId() != ownerId) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store order");
		}
		return order;
	}

	private InstantRange todaySeoulRange() {
		LocalDate today = LocalDate.now(SEOUL);
		Instant start = today.atStartOfDay(SEOUL).toInstant();
		Instant end = today.plusDays(1).atStartOfDay(SEOUL).toInstant();
		return new InstantRange(start, end);
	}

	private record InstantRange(Instant start, Instant end) {
	}
}
