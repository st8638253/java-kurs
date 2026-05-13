package ua.edu.duikt.restaurant.dto.response;

import lombok.Getter;
import ua.edu.duikt.restaurant.entity.MenuCategory;

@Getter
public class MenuCategoryResponse {

    private Long id;
    private String name;
    private Integer sortOrder;

    public static MenuCategoryResponse from(MenuCategory category) {
        MenuCategoryResponse response = new MenuCategoryResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.sortOrder = category.getSortOrder();
        return response;
    }
}
