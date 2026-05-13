package ua.edu.duikt.restaurant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.edu.duikt.restaurant.entity.enums.PaymentMethod;

@Data
public class CreatePaymentRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private PaymentMethod method;

    @NotNull
    private Long cashierId;
}
