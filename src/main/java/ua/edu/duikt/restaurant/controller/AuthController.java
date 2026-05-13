package ua.edu.duikt.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ua.edu.duikt.restaurant.dto.request.LoginRequest;
import ua.edu.duikt.restaurant.dto.response.LoginResponse;
import ua.edu.duikt.restaurant.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Автентифікація", description = "Отримання JWT-токена")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вхід у систему",
            description = "Перевіряє логін та пароль працівника і повертає JWT-токен з його роллю. Доступно без автентифікації.")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}
