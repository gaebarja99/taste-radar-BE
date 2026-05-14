package com.tasteradar.domain.order.service;

import com.tasteradar.domain.cart.repository.CartRepository;
import com.tasteradar.domain.order.api.dto.OrderCreateRequest;
import com.tasteradar.domain.order.api.dto.OrderDetailResponse;
import com.tasteradar.domain.order.api.dto.OrderItemResponse;
import com.tasteradar.domain.order.api.dto.OrderSummaryResponse;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderItem;
import com.tasteradar.domain.order.entity.OrderStatus;
import com.tasteradar.domain.order.repository.FoodOrderRepository;
import com.tasteradar.domain.review.repository.ReviewRepository;
import com.tasteradar.domain.payment.repository.PaymentRepository;
import com.tasteradar.domain.payment.service.KakaoPayService;
import com.tasteradar.domain.user.entity.UserRole;
import com.tasteradar.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final FoodOrderRepository foodOrderRepository;
	private final CartRepository cartRepository;
	private final UserRepository userRepository;
	private final PaymentRepository paymentRepository;
	private final KakaoPayService kakaoPayService;
	private final ReviewRepository reviewRepository;

	@Transactional
	public OrderDetailResponse create(long userId, OrderCreateRequest request) {
		var cart = cartRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty"));
		if (cart.getItems().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
		}
		if (cart.getStore().getId() != request.storeId()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Store does not match cart");
		}
		if (cart.getStore().isDeleted()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		int total = cart.getItems().stream()
				.mapToInt(ci -> (int) (ci.getMenu().getPrice() * ci.getQuantity()))
				.sum();
		if (total < cart.getStore().getMinOrderAmount()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Below minimum order amount");
		}
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		FoodOrder order = new FoodOrder();
		order.setUser(user);
		order.setStore(cart.getStore());
		order.setOrderNumber("ORD-" + UUID.randomUUID());
		order.setZipCode(request.zipCode());
		order.setAddress(request.address());
		order.setAddressDetail(request.addressDetail());
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTotalAmount(total);
		for (var ci : cart.getItems()) {
			OrderItem oi = new OrderItem();
			oi.setOrder(order);
			oi.setMenu(ci.getMenu());
			oi.setQuantity(ci.getQuantity());
			oi.setPrice((int) ci.getMenu().getPrice());
			order.getItems().add(oi);
		}
		foodOrderRepository.save(order);
		cart.getItems().clear();
		return toDetail(order);
	}

	@Transactional(readOnly = true)
	public Page<OrderSummaryResponse> myOrders(long userId, Pageable pageable) {
		return foodOrderRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
				.map(order -> OrderSummaryMapper.toSummary(
						order,
						reviewRepository.existsByOrder_Id(order.getId())
				));
	}

	@Transactional(readOnly = true)
	public OrderDetailResponse detail(long userId, UserRole role, long orderId) {
		FoodOrder order = (role == UserRole.OWNER)
				? foodOrderRepository.findByIdAndStore_Owner_Id(orderId, userId)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"))
				: foodOrderRepository.findByIdAndUser_Id(orderId, userId)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		return toDetail(order);
	}

	@Transactional
	public void cancel(long userId, long orderId, String reason) {
		FoodOrder order = foodOrderRepository.findByIdAndUser_Id(orderId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		if (order.getOrderStatus() == OrderStatus.CANCELED) {
			return;
		}
		if (order.getOrderStatus() != OrderStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING orders can be cancelled");
		}
		String cancelReason = (reason == null || reason.isBlank()) ? "고객 주문 취소" : reason.trim();
		kakaoPayService.cancelApprovedForOrder(order, cancelReason);
		order.setOrderStatus(OrderStatus.CANCELED);
	}

	private OrderDetailResponse toDetail(FoodOrder o) {
		var items = o.getItems().stream()
				.map(oi -> new OrderItemResponse(
						oi.getMenu().getId(),
						oi.getMenu().getName(),
						oi.getQuantity(),
						oi.getPrice(),
						oi.getPrice() * oi.getQuantity()
				))
				.toList();
		return new OrderDetailResponse(
				o.getId(),
				o.getStore().getId(),
				o.getStore().getName(),
				o.getOrderStatus(),
				o.getZipCode(),
				o.getAddress(),
				o.getAddressDetail(),
				o.getTotalAmount(),
				o.getRejectionReason(),
				o.getCreatedAt(),
				items
		);
	}
}
