package ua.edu.duikt.restaurant.dto.response;

import lombok.Getter;
import ua.edu.duikt.restaurant.entity.RestaurantTable;
import ua.edu.duikt.restaurant.entity.enums.TableStatus;

@Getter
public class RestaurantTableResponse {

    private Long id;
    private int number;
    private int capacity;
    private TableStatus status;

    public static RestaurantTableResponse from(RestaurantTable table) {
        RestaurantTableResponse response = new RestaurantTableResponse();
        response.id = table.getId();
        response.number = table.getNumber();
        response.capacity = table.getCapacity();
        response.status = table.getStatus();
        return response;
    }
}
