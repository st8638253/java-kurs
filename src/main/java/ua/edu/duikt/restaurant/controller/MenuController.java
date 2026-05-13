package ua.edu.duikt.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.duikt.restaurant.dto.request.CreateMenuItemRequest;
import ua.edu.duikt.restaurant.dto.response.MenuCategoryResponse;
import ua.edu.duikt.restaurant.dto.response.MenuItemResponse;
import ua.edu.duikt.restaurant.entity.MenuCategory;
import ua.edu.duikt.restaurant.entity.MenuItem;
import ua.edu.duikt.restaurant.service.MenuService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Tag(name = "Меню", description = "Управління стравами та категоріями")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Список доступних страв",
            description = "Повертає всі страви з isAvailable = true. Доступно будь-якому автентифікованому користувачу.")
    public List<MenuItemResponse> listAvailableItems() {
        List<MenuItemResponse> result = new ArrayList<>();
        for (MenuItem item : menuService.listAvailable()) {
            result.add(MenuItemResponse.from(item));
        }
        return result;
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Список категорій меню",
            description = "Повертає всі категорії меню. Доступно будь-якому автентифікованому користувачу.")
    public List<MenuCategoryResponse> listCategories() {
        List<MenuCategoryResponse> result = new ArrayList<>();
        for (MenuCategory category : menuService.listAllCategories()) {
            result.add(MenuCategoryResponse.from(category));
        }
        return result;
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Створити страву",
            description = "Додає нову страву до меню. Доступно лише адміністратору (ADMIN).")
    public MenuItemResponse createItem(@RequestBody @Valid CreateMenuItemRequest request) {
        return MenuItemResponse.from(menuService.createItem(request));
    }

    @PatchMapping("/items/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Змінити доступність страви",
            description = "Вмикає або вимикає доступність страви. Доступно лише адміністратору (ADMIN).")
    public MenuItemResponse setAvailability(@PathVariable Long id,
                                            @RequestParam boolean available) {
        return MenuItemResponse.from(menuService.setAvailability(id, available));
    }
}
