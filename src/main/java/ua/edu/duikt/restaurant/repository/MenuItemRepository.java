package ua.edu.duikt.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.duikt.restaurant.entity.MenuItem;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByIsAvailableTrue();
}
