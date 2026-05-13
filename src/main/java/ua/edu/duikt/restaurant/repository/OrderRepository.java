package ua.edu.duikt.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.duikt.restaurant.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
