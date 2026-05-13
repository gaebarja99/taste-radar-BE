package com.tasteradar.domain.payment.repository;

import com.tasteradar.domain.payment.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrder_Id(Long orderId);

	Optional<Payment> findByOrder_IdAndOrder_User_Id(Long orderId, Long userId);
}
