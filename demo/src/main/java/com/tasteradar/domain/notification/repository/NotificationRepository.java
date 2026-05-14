package com.tasteradar.domain.notification.repository;

import com.tasteradar.domain.notification.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

	Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

	boolean existsByOrder_IdAndOrderStatus(Long orderId, com.tasteradar.domain.order.entity.OrderStatus orderStatus);

	long countByUser_IdAndIsReadFalse(Long userId);

	@Modifying
	@Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
	int markAllReadByUser_Id(@Param("userId") long userId);
}