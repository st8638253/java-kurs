package ua.edu.duikt.restaurant.dto.response;

import lombok.Getter;
import ua.edu.duikt.restaurant.entity.OrderItem;
import ua.edu.duikt.restaurant.entity.enums.OrderItemStatus;

import java.math.BigDecimal;

@Getter
public class OrderItemResponse {

    private Long id;
    private String menuItemName;
    private int quantity;
    private BigDecimal unitPrice;
    private OrderItemStatus status;
    private String note;
    private String cancelReason;

    public static OrderItemResponse from(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.id = item.getId();
        response.menuItemName = item.getMenuItem().getName();
        response.quantity = item.getQuantity();
        response.unitPrice = item.getUnitPrice();
        response.status = item.getStatus();
        response.note = item.getNote();
        response.cancelReason = item.getCancelReason();
        return response;
    }
}
