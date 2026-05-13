package ua.edu.duikt.restaurant.dto.response;

import lombok.Getter;
import ua.edu.duikt.restaurant.entity.MenuItem;

import java.math.BigDecimal;

@Getter
public class MenuItemResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean isAvailable;
    private Long categoryId;
    private String categoryName;

    public static MenuItemResponse from(MenuItem item) {
        MenuItemResponse response = new MenuItemResponse();
        response.id = item.getId();
        response.name = item.getName();
        response.description = item.getDescription();
        response.price = item.getPrice();
        response.isAvailable = item.isAvailable();
        response.categoryId = item.getCategory().getId();
        response.categoryName = item.getCategory().getName();
        return response;
    }
}
