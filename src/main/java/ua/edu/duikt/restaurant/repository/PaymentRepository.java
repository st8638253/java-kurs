package ua.edu.duikt.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.duikt.restaurant.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
