package ua.edu.duikt.restaurant.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.edu.duikt.restaurant.dto.request.CreateMenuItemRequest;
import ua.edu.duikt.restaurant.entity.MenuCategory;
import ua.edu.duikt.restaurant.entity.MenuItem;
import ua.edu.duikt.restaurant.exception.NotFoundException;
import ua.edu.duikt.restaurant.repository.MenuCategoryRepository;
import ua.edu.duikt.restaurant.repository.MenuItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private MenuCategoryRepository menuCategoryRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void createItem_shouldCreate_whenDataIsValid() {
        MenuCategory category = buildCategory(1L, "Супи");
        CreateMenuItemRequest request = buildRequest("Борщ", BigDecimal.valueOf(89), 1L);

        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = menuService.createItem(request);

        assertThat(result.getName()).isEqualTo("Борщ");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(89));
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getCategory()).isEqualTo(category);
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void createItem_shouldThrowNotFoundException_whenCategoryDoesNotExist() {
        CreateMenuItemRequest request = buildRequest("Борщ", BigDecimal.valueOf(89), 99L);

        when(menuCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.createItem(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(menuItemRepository, never()).save(any());
    }

    @Test
    void setAvailability_shouldUpdate_whenItemExists() {
        MenuItem item = buildItem(1L, true);

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(menuItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = menuService.setAvailability(1L, false);

        assertThat(result.isAvailable()).isFalse();
        verify(menuItemRepository).save(item);
    }

    @Test
    void setAvailability_shouldThrowNotFoundException_whenItemDoesNotExist() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.setAvailability(99L, false))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void listAvailable_shouldReturnOnlyAvailableItems() {
        List<MenuItem> available = List.of(buildItem(1L, true), buildItem(2L, true));

        when(menuItemRepository.findByIsAvailableTrue()).thenReturn(available);

        List<MenuItem> result = menuService.listAvailable();

        assertThat(result).hasSize(2);
        for (MenuItem item : result) {
            assertThat(item.isAvailable()).isTrue();
        }
    }

    @Test
    void getItemById_shouldReturn_whenExists() {
        MenuItem item = buildItem(1L, true);

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        MenuItem result = menuService.getItemById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getItemById_shouldThrowNotFoundException_whenItemDoesNotExist() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getItemById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    private CreateMenuItemRequest buildRequest(String name, BigDecimal price, Long categoryId) {
        CreateMenuItemRequest request = new CreateMenuItemRequest();
        request.setName(name);
        request.setPrice(price);
        request.setCategoryId(categoryId);
        return request;
    }

    private MenuItem buildItem(Long id, boolean available) {
        MenuItem item = new MenuItem();
        item.setId(id);
        item.setName("Тест");
        item.setPrice(BigDecimal.valueOf(100));
        item.setAvailable(available);
        return item;
    }

    private MenuCategory buildCategory(Long id, String name) {
        MenuCategory category = new MenuCategory();
        category.setId(id);
        category.setName(name);
        category.setSortOrder(1);
        return category;
    }
}
