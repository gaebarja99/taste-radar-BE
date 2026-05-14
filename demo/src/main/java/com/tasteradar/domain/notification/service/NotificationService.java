package com.tasteradar.domain.notification.service;

import com.tasteradar.domain.notification.api.dto.NotificationResponse;
import com.tasteradar.domain.notification.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	@Transactional(readOnly = true)
	public List<NotificationResponse> list(long userId) {
		return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
				.map(n -> new NotificationResponse(
						n.getId(),
						n.getOrder().getId(),
						n.getOrderStatus(),
						n.getMessage(),
						n.isRead(),
						n.getCreatedAt()
				))
				.toList();
	}

	@Transactional
	public void markRead(long userId, long notificationId) {
		var n = notificationRepository.findByIdAndUser_Id(notificationId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
		n.setRead(true);
	}

	@Transactional
	public void markAllRead(long userId) {
		notificationRepository.markAllReadByUser_Id(userId);
	}

	@Transactional(readOnly = true)
	public long unreadCount(long userId) {
		return notificationRepository.countByUser_IdAndIsReadFalse(userId);
	}
}
