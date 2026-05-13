package ua.edu.duikt.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.duikt.restaurant.dto.request.CreatePaymentRequest;
import ua.edu.duikt.restaurant.dto.response.PaymentResponse;
import ua.edu.duikt.restaurant.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Оплата", description = "Формування чеку та проведення оплати")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CASHIER')")
    @Operation(summary = "Закрити замовлення та провести оплату",
            description = "Перевіряє, що всі позиції подані або скасовані, обчислює суму, створює оплату та закриває замовлення. Доступно лише касиру (CASHIER).")
    public PaymentResponse closeOrderAndPay(@RequestBody @Valid CreatePaymentRequest request) {
        return PaymentResponse.from(paymentService.closeOrderAndPay(request));
    }
}
