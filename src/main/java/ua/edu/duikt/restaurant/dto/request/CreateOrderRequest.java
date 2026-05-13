package ua.edu.duikt.restaurant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull
    private Long tableId;

    @NotNull
    private Long waiterId;
}
