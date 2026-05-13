package ua.edu.duikt.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.duikt.restaurant.entity.MenuCategory;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
}
