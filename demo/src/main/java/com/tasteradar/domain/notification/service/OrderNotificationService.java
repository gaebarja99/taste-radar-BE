package com.tasteradar.domain.notification.service;

import com.tasteradar.domain.notification.entity.Notification;
import com.tasteradar.domain.notification.repository.NotificationRepository;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderStatus;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

	private static final Set<OrderStatus> NOTIFY_STATUSES = EnumSet.of(
			OrderStatus.PENDING,
			OrderStatus.COOKING,
			OrderStatus.DELIVERING,
			OrderStatus.DELIVERED
	);

	private final NotificationRepository notificationRepository;
	private final KakaoTalkOrderMessageService kakaoTalkOrderMessageService;

	@Transactional
	public void notifyOrderStatus(FoodOrder order, OrderStatus status) {
		if (!NOTIFY_STATUSES.contains(status)) {
			return;
		}
		if (notificationRepository.existsByOrder_IdAndOrderStatus(order.getId(), status)) {
			return;
		}

		String message = buildMessage(order, status);
		Notification notification = new Notification();
		notification.setOrder(order);
		notification.setUser(order.getUser());
		notification.setOrderStatus(status);
		notification.setMessage(message);
		notification.setRead(false);
		notificationRepository.save(notification);

		kakaoTalkOrderMessageService.send(order, status, message);
	}

	private String buildMessage(FoodOrder order, OrderStatus status) {
		String storeName = order.getStore().getName();
		return switch (status) {
			case PENDING -> storeName + " 주문이 접수되었어요. 가게에서 확인 중입니다.";
			case COOKING -> storeName + "에서 조리를 시작했어요.";
			case DELIVERING -> storeName + " 배달이 출발했어요.";
			case DELIVERED -> storeName + " 배달이 완료되었어요. 맛있게 드세요!";
			default -> storeName + " 주문 상태가 " + status.getDescription() + "(으)로 변경되었어요.";
		};
	}
}
