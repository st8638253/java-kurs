package ua.edu.duikt.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.duikt.restaurant.dto.request.AddItemRequest;
import ua.edu.duikt.restaurant.dto.request.CancelItemRequest;
import ua.edu.duikt.restaurant.dto.request.CreateOrderRequest;
import ua.edu.duikt.restaurant.dto.request.UpdateItemStatusRequest;
import ua.edu.duikt.restaurant.dto.response.OrderItemResponse;
import ua.edu.duikt.restaurant.dto.response.OrderResponse;
import ua.edu.duikt.restaurant.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Замовлення", description = "Створення замовлень та управління позиціями")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('WAITER')")
    @Operation(summary = "Відкрити замовлення",
            description = "Створює нове замовлення на вказаний столик. Доступно лише офіціанту (WAITER).")
    public OrderResponse openOrder(@RequestBody @Valid CreateOrderRequest request) {
        return OrderResponse.from(orderService.openOrder(request));
    }

    @PostMapping("/{orderId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('WAITER')")
    @Operation(summary = "Додати позицію до замовлення",
            description = "Додає страву до відкритого замовлення з фіксацією поточної ціни. Доступно лише офіціанту (WAITER).")
    public OrderItemResponse addItem(@PathVariable Long orderId,
                                     @RequestBody @Valid AddItemRequest request) {
        return OrderItemResponse.from(orderService.addItem(orderId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('WAITER')")
    @Operation(summary = "Скасувати позицію замовлення",
            description = "Скасовує позицію зі статусом NEW або COOKING з обов'язковим зазначенням причини. Доступно лише офіціанту (WAITER).")
    public void cancelItem(@PathVariable Long itemId,
                           @RequestBody @Valid CancelItemRequest request) {
        orderService.cancelItem(itemId, request);
    }

    @PatchMapping("/items/{itemId}/status")
    @PreAuthorize("hasRole('COOK')")
    @Operation(summary = "Оновити статус позиції",
            description = "Переводить позицію по життєвому циклу NEW → COOKING → READY → SERVED. Доступно лише кухарю (COOK).")
    public OrderItemResponse updateItemStatus(@PathVariable Long itemId,
                                              @RequestBody @Valid UpdateItemStatusRequest request) {
        return OrderItemResponse.from(orderService.updateItemStatus(itemId, request));
    }
}
