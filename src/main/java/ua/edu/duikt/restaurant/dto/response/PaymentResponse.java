package ua.edu.duikt.restaurant.dto.response;

import lombok.Getter;
import ua.edu.duikt.restaurant.entity.Payment;
import ua.edu.duikt.restaurant.entity.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private String cashierName;
    private BigDecimal amount;
    private PaymentMethod method;
    private LocalDateTime paidAt;

    public static PaymentResponse from(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.id = payment.getId();
        response.orderId = payment.getOrder().getId();
        response.cashierName = payment.getCashier().getFullName();
        response.amount = payment.getAmount();
        response.method = payment.getMethod();
        response.paidAt = payment.getPaidAt();
        return response;
    }
}
