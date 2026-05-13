package ua.edu.duikt.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.edu.duikt.restaurant.entity.OrderItem;
import ua.edu.duikt.restaurant.entity.enums.OrderItemStatus;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.status IN :statuses ORDER BY oi.order.createdAt ASC")
    List<OrderItem> findByStatusIn(List<OrderItemStatus> statuses);
}
