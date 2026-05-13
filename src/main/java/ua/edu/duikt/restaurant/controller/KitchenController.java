package ua.edu.duikt.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.duikt.restaurant.dto.response.OrderItemResponse;
import ua.edu.duikt.restaurant.entity.OrderItem;
import ua.edu.duikt.restaurant.service.OrderService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
@Tag(name = "Кухня", description = "Черга страв на приготування")
public class KitchenController {

    private final OrderService orderService;

    @GetMapping("/queue")
    @PreAuthorize("hasRole('COOK')")
    @Operation(summary = "Черга кухні",
            description = "Повертає всі позиції у статусах NEW або COOKING, відсортовані за часом створення замовлення. Доступно лише кухарю (COOK).")
    public List<OrderItemResponse> getKitchenQueue() {
        List<OrderItemResponse> result = new ArrayList<>();
        for (OrderItem item : orderService.getKitchenQueue()) {
            result.add(OrderItemResponse.from(item));
        }
        return result;
    }
}
