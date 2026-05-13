package ua.edu.duikt.restaurant.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import ua.edu.duikt.restaurant.entity.Employee;
import ua.edu.duikt.restaurant.entity.enums.EmployeeRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateToken_shouldCreateValidToken() {
        Employee employee = buildEmployee("waiter", EmployeeRole.WAITER);

        String token = jwtService.generateToken(employee);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void validateToken_shouldReturnClaims_whenTokenIsValid() {
        Employee employee = buildEmployee("admin", EmployeeRole.ADMIN);
        String token = jwtService.generateToken(employee);

        Claims claims = jwtService.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());

        assertThat(jwtService.getLoginFromToken(token)).isEqualTo("admin");
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    void validateToken_shouldThrowException_whenTokenIsInvalid() {
        String garbage = "not-a-real-jwt-token";

        assertThatThrownBy(() -> jwtService.validateToken(garbage))
                .isInstanceOf(JwtException.class);
    }

    private Employee buildEmployee(String login, EmployeeRole role) {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFullName("Тест");
        employee.setLogin(login);
        employee.setPasswordHash("x");
        employee.setRole(role);
        employee.setActive(true);
        return employee;
    }
}
