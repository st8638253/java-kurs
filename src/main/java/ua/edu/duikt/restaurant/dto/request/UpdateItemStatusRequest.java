package ua.edu.duikt.restaurant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.edu.duikt.restaurant.entity.enums.OrderItemStatus;

@Data
public class UpdateItemStatusRequest {

    @NotNull
    private OrderItemStatus status;
}
