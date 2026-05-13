package ua.edu.duikt.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.duikt.restaurant.entity.Employee;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByLogin(String login);
}
