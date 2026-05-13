package ua.edu.duikt.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.duikt.restaurant.dto.request.CreateMenuItemRequest;
import ua.edu.duikt.restaurant.entity.MenuCategory;
import ua.edu.duikt.restaurant.entity.MenuItem;
import ua.edu.duikt.restaurant.exception.NotFoundException;
import ua.edu.duikt.restaurant.repository.MenuCategoryRepository;
import ua.edu.duikt.restaurant.repository.MenuItemRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    @Transactional
    public MenuItem createItem(CreateMenuItemRequest dto) {
        Optional<MenuCategory> categoryOpt = menuCategoryRepository.findById(dto.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new NotFoundException("MenuCategory with id " + dto.getCategoryId() + " not found");
        }
        MenuCategory category = categoryOpt.get();

        MenuItem item = new MenuItem();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCategory(category);
        item.setAvailable(true);

        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem setAvailability(Long itemId, boolean available) {
        Optional<MenuItem> itemOpt = menuItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("MenuItem with id " + itemId + " not found");
        }
        MenuItem item = itemOpt.get();

        item.setAvailable(available);
        return menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> listAvailable() {
        return menuItemRepository.findByIsAvailableTrue();
    }

    @Transactional(readOnly = true)
    public List<MenuCategory> listAllCategories() {
        return menuCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MenuItem getItemById(Long id) {
        Optional<MenuItem> itemOpt = menuItemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("MenuItem with id " + id + " not found");
        }
        return itemOpt.get();
    }
}
