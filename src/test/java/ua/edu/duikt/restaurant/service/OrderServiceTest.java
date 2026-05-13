package ua.edu.duikt.restaurant.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.edu.duikt.restaurant.dto.request.AddItemRequest;
import ua.edu.duikt.restaurant.dto.request.CancelItemRequest;
import ua.edu.duikt.restaurant.dto.request.CreateOrderRequest;
import ua.edu.duikt.restaurant.dto.request.UpdateItemStatusRequest;
import ua.edu.duikt.restaurant.entity.Employee;
import ua.edu.duikt.restaurant.entity.MenuItem;
import ua.edu.duikt.restaurant.entity.Order;
import ua.edu.duikt.restaurant.entity.OrderItem;
import ua.edu.duikt.restaurant.entity.RestaurantTable;
import ua.edu.duikt.restaurant.entity.enums.EmployeeRole;
import ua.edu.duikt.restaurant.entity.enums.OrderItemStatus;
import ua.edu.duikt.restaurant.entity.enums.OrderStatus;
import ua.edu.duikt.restaurant.entity.enums.TableStatus;
import ua.edu.duikt.restaurant.exception.BusinessRuleException;
import ua.edu.duikt.restaurant.repository.EmployeeRepository;
import ua.edu.duikt.restaurant.repository.MenuItemRepository;
import ua.edu.duikt.restaurant.repository.OrderItemRepository;
import ua.edu.duikt.restaurant.repository.OrderRepository;
import ua.edu.duikt.restaurant.repository.RestaurantTableRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private RestaurantTableRepository tableRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private MenuItemRepository menuItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void openOrder_shouldCreateOrder_whenTableIsFree() {
        RestaurantTable table = buildTable(1L, TableStatus.FREE);
        Employee waiter = buildEmployee(1L);
        CreateOrderRequest request = buildCreateOrderRequest(1L, 1L);

        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(waiter));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tableRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.openOrder(request);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.OPEN);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(table.getStatus()).isEqualTo(TableStatus.OCCUPIED);
        verify(orderRepository).save(any(Order.class));
        verify(tableRepository).save(table);
    }

    @Test
    void openOrder_shouldThrow_whenTableIsOccupied() {
        RestaurantTable table = buildTable(1L, TableStatus.OCCUPIED);
        CreateOrderRequest request = buildCreateOrderRequest(1L, 1L);

        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        assertThatThrownBy(() -> orderService.openOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("OCCUPIED");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void addItem_shouldAddItem_whenOrderIsOpen() {
        Order order = buildOrder(1L, OrderStatus.OPEN);
        MenuItem menuItem = buildMenuItem(1L, BigDecimal.valueOf(89), true);
        AddItemRequest request = buildAddItemRequest(1L, 2);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderItem result = orderService.addItem(1L, request);

        assertThat(result.getStatus()).isEqualTo(OrderItemStatus.NEW);
        assertThat(result.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(89));
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(178));
    }

    @Test
    void addItem_shouldThrow_whenOrderIsClosed() {
        Order order = buildOrder(1L, OrderStatus.CLOSED);
        AddItemRequest request = buildAddItemRequest(1L, 1);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.addItem(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void addItem_shouldThrow_whenMenuItemNotAvailable() {
        Order order = buildOrder(1L, OrderStatus.OPEN);
        MenuItem menuItem = buildMenuItem(1L, BigDecimal.valueOf(89), false);
        AddItemRequest request = buildAddItemRequest(1L, 1);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        assertThatThrownBy(() -> orderService.addItem(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void cancelItem_shouldCancel_whenStatusIsNew() {
        Order order = buildOrder(1L, OrderStatus.OPEN);
        OrderItem item = buildOrderItem(1L, OrderItemStatus.NEW, order, BigDecimal.valueOf(89), 1);
        order.getItems().add(item);
        CancelItemRequest request = buildCancelRequest("Клієнт відмовився");

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderItem result = orderService.cancelItem(1L, request);

        assertThat(result.getStatus()).isEqualTo(OrderItemStatus.CANCELLED);
        assertThat(result.getCancelReason()).isEqualTo("Клієнт відмовився");
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void cancelItem_shouldThrow_whenStatusIsServed() {
        Order order = buildOrder(1L, OrderStatus.OPEN);
        OrderItem item = buildOrderItem(1L, OrderItemStatus.SERVED, order, BigDecimal.valueOf(89), 1);
        CancelItemRequest request = buildCancelRequest("Причина");

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> orderService.cancelItem(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("SERVED");
    }

    @Test
    void cancelItem_shouldThrow_whenReasonIsBlank() {
        Order order = buildOrder(1L, OrderStatus.OPEN);
        OrderItem item = buildOrderItem(1L, OrderItemStatus.NEW, order, BigDecimal.valueOf(89), 1);
        CancelItemRequest request = buildCancelRequest("   ");

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> orderService.cancelItem(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void updateItemStatus_shouldUpdate_fromNewToCooking() {
        Order order = buildOrder(1L, OrderStatus.OPEN);
        OrderItem item = buildOrderItem(1L, OrderItemStatus.NEW, order, BigDecimal.valueOf(89), 1);
        order.getItems().add(item);
        UpdateItemStatusRequest request = buildUpdateStatusRequest(OrderItemStatus.COOKING);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderItem result = orderService.updateItemStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(OrderItemStatus.COOKING);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void updateItemStatus_shouldSetOrderToReady_whenAllItemsAreReady() {
        Order order = buildOrder(1L, OrderStatus.IN_PROGRESS);
        OrderItem item = buildOrderItem(1L, OrderItemStatus.COOKING, order, BigDecimal.valueOf(89), 1);
        order.getItems().add(item);
        UpdateItemStatusRequest request = buildUpdateStatusRequest(OrderItemStatus.READY);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateItemStatus(1L, request);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void updateItemStatus_shouldUpdate_fromReadyToServed() {
        Order order = buildOrder(1L, OrderStatus.READY);
        OrderItem item = buildOrderItem(1L, OrderItemStatus.READY, order, BigDecimal.valueOf(89), 1);
        order.getItems().add(item);
        UpdateItemStatusRequest request = buildUpdateStatusRequest(OrderItemStatus.SERVED);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderItem result = orderService.updateItemStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(OrderItemStatus.SERVED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SERVED);
    }

    @Test
    void updateItemStatus_shouldThrow_whenInvalidTransition() {
        Order order = buildOrder(1L, OrderStatus.OPEN);
        OrderItem item = buildOrderItem(1L, OrderItemStatus.NEW, order, BigDecimal.valueOf(89), 1);
        UpdateItemStatusRequest request = buildUpdateStatusRequest(OrderItemStatus.READY);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> orderService.updateItemStatus(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("NEW")
                .hasMessageContaining("READY");
    }

    @Test
    void getKitchenQueue_shouldReturnNewAndCookingItems() {
        List<OrderItem> queue = List.of(
                buildOrderItem(1L, OrderItemStatus.NEW, buildOrder(1L, OrderStatus.OPEN),
                        BigDecimal.valueOf(89), 1),
                buildOrderItem(2L, OrderItemStatus.COOKING, buildOrder(2L, OrderStatus.OPEN),
                        BigDecimal.valueOf(65), 1)
        );

        when(orderItemRepository.findByStatusIn(
                List.of(OrderItemStatus.NEW, OrderItemStatus.COOKING)))
                .thenReturn(queue);

        List<OrderItem> result = orderService.getKitchenQueue();

        assertThat(result).hasSize(2);
        for (OrderItem i : result) {
            assertThat(i.getStatus() == OrderItemStatus.NEW
                    || i.getStatus() == OrderItemStatus.COOKING).isTrue();
        }
    }

    private RestaurantTable buildTable(Long id, TableStatus status) {
        RestaurantTable table = new RestaurantTable();
        table.setId(id);
        table.setNumber(id.intValue());
        table.setCapacity(4);
        table.setStatus(status);
        return table;
    }

    private Employee buildEmployee(Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFullName("Тест Офіціант");
        employee.setLogin("test");
        employee.setPasswordHash("x");
        employee.setRole(EmployeeRole.WAITER);
        employee.setActive(true);
        return employee;
    }

    private Order buildOrder(Long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }

    private MenuItem buildMenuItem(Long id, BigDecimal price, boolean available) {
        MenuItem item = new MenuItem();
        item.setId(id);
        item.setName("Страва");
        item.setPrice(price);
        item.setAvailable(available);
        return item;
    }

    private OrderItem buildOrderItem(Long id, OrderItemStatus status, Order order,
                                     BigDecimal unitPrice, int quantity) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setStatus(status);
        item.setOrder(order);
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setMenuItem(buildMenuItem(1L, unitPrice, true));
        return item;
    }

    private CreateOrderRequest buildCreateOrderRequest(Long tableId, Long waiterId) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setTableId(tableId);
        request.setWaiterId(waiterId);
        return request;
    }

    private AddItemRequest buildAddItemRequest(Long menuItemId, int quantity) {
        AddItemRequest request = new AddItemRequest();
        request.setMenuItemId(menuItemId);
        request.setQuantity(quantity);
        return request;
    }

    private CancelItemRequest buildCancelRequest(String reason) {
        CancelItemRequest request = new CancelItemRequest();
        request.setReason(reason);
        return request;
    }

    private UpdateItemStatusRequest buildUpdateStatusRequest(OrderItemStatus status) {
        UpdateItemStatusRequest request = new UpdateItemStatusRequest();
        request.setStatus(status);
        return request;
    }
}
