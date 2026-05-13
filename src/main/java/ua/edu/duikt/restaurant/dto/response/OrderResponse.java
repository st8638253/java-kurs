package ua.edu.duikt.restaurant.dto.response;

import lombok.Getter;
import ua.edu.duikt.restaurant.entity.Order;
import ua.edu.duikt.restaurant.entity.OrderItem;
import ua.edu.duikt.restaurant.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class OrderResponse {

    private Long id;
    private int tableNumber;
    private String waiterName;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.id = order.getId();
        response.tableNumber = order.getTable().getNumber();
        response.waiterName = order.getWaiter().getFullName();
        response.status = order.getStatus();
        response.createdAt = order.getCreatedAt();
        response.closedAt = order.getClosedAt();
        response.totalAmount = order.getTotalAmount();

        List<OrderItemResponse> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            items.add(OrderItemResponse.from(item));
        }
        response.items = items;

        return response;
    }
}
