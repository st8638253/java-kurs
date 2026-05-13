package ua.edu.duikt.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ua.edu.duikt.restaurant.dto.request.LoginRequest;
import ua.edu.duikt.restaurant.dto.response.LoginResponse;
import ua.edu.duikt.restaurant.entity.Employee;
import ua.edu.duikt.restaurant.exception.BusinessRuleException;
import ua.edu.duikt.restaurant.repository.EmployeeRepository;
import ua.edu.duikt.restaurant.security.JwtService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Optional<Employee> employeeOpt = employeeRepository.findByLogin(request.getLogin());
        if (employeeOpt.isEmpty()) {
            throw new BusinessRuleException("Invalid credentials");
        }
        Employee employee = employeeOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), employee.getPasswordHash())) {
            throw new BusinessRuleException("Invalid credentials");
        }

        String token = jwtService.generateToken(employee);
        return new LoginResponse(token);
    }
}
