package ua.edu.duikt.restaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelItemRequest {

    @NotBlank
    private String reason;
}
