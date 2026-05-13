package ua.edu.duikt.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.duikt.restaurant.dto.response.RestaurantTableResponse;
import ua.edu.duikt.restaurant.entity.RestaurantTable;
import ua.edu.duikt.restaurant.service.TableService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@Tag(name = "Столики", description = "Перегляд столиків залу")
public class TableController {

    private final TableService tableService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Список усіх столиків",
            description = "Повертає всі столики з їх поточним статусом. Доступно будь-якому автентифікованому користувачу.")
    public List<RestaurantTableResponse> listAllTables() {
        List<RestaurantTableResponse> result = new ArrayList<>();
        for (RestaurantTable table : tableService.listAllTables()) {
            result.add(RestaurantTableResponse.from(table));
        }
        return result;
    }

    @GetMapping("/free")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Список вільних столиків",
            description = "Повертає лише столики у статусі FREE. Доступно будь-якому автентифікованому користувачу.")
    public List<RestaurantTableResponse> getFreeTables() {
        List<RestaurantTableResponse> result = new ArrayList<>();
        for (RestaurantTable table : tableService.getFreeTables()) {
            result.add(RestaurantTableResponse.from(table));
        }
        return result;
    }
}
