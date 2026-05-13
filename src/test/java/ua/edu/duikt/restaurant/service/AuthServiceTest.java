package ua.edu.duikt.restaurant.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ua.edu.duikt.restaurant.dto.request.LoginRequest;
import ua.edu.duikt.restaurant.dto.response.LoginResponse;
import ua.edu.duikt.restaurant.entity.Employee;
import ua.edu.duikt.restaurant.entity.enums.EmployeeRole;
import ua.edu.duikt.restaurant.exception.BusinessRuleException;
import ua.edu.duikt.restaurant.repository.EmployeeRepository;
import ua.edu.duikt.restaurant.security.JwtService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        Employee employee = buildEmployee("waiter", "$2b$10$hash", EmployeeRole.WAITER);
        LoginRequest request = buildRequest("waiter", "waiter123");

        when(employeeRepository.findByLogin("waiter")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("waiter123", "$2b$10$hash")).thenReturn(true);
        when(jwtService.generateToken(employee)).thenReturn("a.b.c");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("a.b.c");
        verify(jwtService).generateToken(employee);
    }

    @Test
    void login_shouldThrow_whenPasswordIsWrong() {
        Employee employee = buildEmployee("waiter", "$2b$10$hash", EmployeeRole.WAITER);
        LoginRequest request = buildRequest("waiter", "wrong");

        when(employeeRepository.findByLogin("waiter")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("wrong", "$2b$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrow_whenLoginNotFound() {
        LoginRequest request = buildRequest("ghost", "any");

        when(employeeRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid credentials");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    private Employee buildEmployee(String login, String passwordHash, EmployeeRole role) {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFullName("Тест");
        employee.setLogin(login);
        employee.setPasswordHash(passwordHash);
        employee.setRole(role);
        employee.setActive(true);
        return employee;
    }

    private LoginRequest buildRequest(String login, String password) {
        LoginRequest request = new LoginRequest();
        request.setLogin(login);
        request.setPassword(password);
        return request;
    }
}
