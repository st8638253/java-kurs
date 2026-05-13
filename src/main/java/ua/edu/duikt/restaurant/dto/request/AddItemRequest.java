package ua.edu.duikt.restaurant.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddItemRequest {

    @NotNull
    private Long menuItemId;

    @Positive
    private int quantity;

    private String note;
}
