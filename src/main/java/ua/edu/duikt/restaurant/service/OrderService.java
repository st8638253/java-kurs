package ua.edu.duikt.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.duikt.restaurant.dto.request.AddItemRequest;
import ua.edu.duikt.restaurant.dto.request.CancelItemRequest;
import ua.edu.duikt.restaurant.dto.request.CreateOrderRequest;
import ua.edu.duikt.restaurant.dto.request.UpdateItemStatusRequest;
import ua.edu.duikt.restaurant.entity.Employee;
import ua.edu.duikt.restaurant.entity.MenuItem;
import ua.edu.duikt.restaurant.entity.Order;
import ua.edu.duikt.restaurant.entity.OrderItem;
import ua.edu.duikt.restaurant.entity.RestaurantTable;
import ua.edu.duikt.restaurant.entity.enums.OrderItemStatus;
import ua.edu.duikt.restaurant.entity.enums.OrderStatus;
import ua.edu.duikt.restaurant.entity.enums.TableStatus;
import ua.edu.duikt.restaurant.exception.BusinessRuleException;
import ua.edu.duikt.restaurant.exception.NotFoundException;
import ua.edu.duikt.restaurant.repository.EmployeeRepository;
import ua.edu.duikt.restaurant.repository.MenuItemRepository;
import ua.edu.duikt.restaurant.repository.OrderItemRepository;
import ua.edu.duikt.restaurant.repository.OrderRepository;
import ua.edu.duikt.restaurant.repository.RestaurantTableRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final EmployeeRepository employeeRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public Order openOrder(CreateOrderRequest dto) {
        Optional<RestaurantTable> tableOpt = tableRepository.findById(dto.getTableId());
        if (tableOpt.isEmpty()) {
            throw new NotFoundException("RestaurantTable with id " + dto.getTableId() + " not found");
        }
        RestaurantTable table = tableOpt.get();

        if (table.getStatus() != TableStatus.FREE) {
            throw new BusinessRuleException(
                    "Table " + table.getNumber() + " is not FREE, current status: " + table.getStatus());
        }

        Optional<Employee> waiterOpt = employeeRepository.findById(dto.getWaiterId());
        if (waiterOpt.isEmpty()) {
            throw new NotFoundException("Employee with id " + dto.getWaiterId() + " not found");
        }
        Employee waiter = waiterOpt.get();

        Order order = new Order();
        order.setTable(table);
        order.setWaiter(waiter);
        order.setStatus(OrderStatus.OPEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);

        orderRepository.save(order);

        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        return order;
    }

    @Transactional
    public OrderItem addItem(Long orderId, AddItemRequest dto) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order with id " + orderId + " not found");
        }
        Order order = orderOpt.get();

        if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Cannot add item to order with status: " + order.getStatus());
        }

        Optional<MenuItem> menuItemOpt = menuItemRepository.findById(dto.getMenuItemId());
        if (menuItemOpt.isEmpty()) {
            throw new NotFoundException("MenuItem with id " + dto.getMenuItemId() + " not found");
        }
        MenuItem menuItem = menuItemOpt.get();

        if (!menuItem.isAvailable()) {
            throw new BusinessRuleException(
                    "MenuItem '" + menuItem.getName() + "' is not available");
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setMenuItem(menuItem);
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(menuItem.getPrice());
        item.setStatus(OrderItemStatus.NEW);
        item.setNote(dto.getNote());

        item = orderItemRepository.save(item);
        order.getItems().add(item);
        recalculateTotal(order);
        recalculateOrderStatus(order);
        orderRepository.save(order);

        return item;
    }

    @Transactional
    public OrderItem cancelItem(Long orderItemId, CancelItemRequest dto) {
        Optional<OrderItem> itemOpt = orderItemRepository.findById(orderItemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("OrderItem with id " + orderItemId + " not found");
        }
        OrderItem item = itemOpt.get();

        if (item.getStatus() != OrderItemStatus.NEW && item.getStatus() != OrderItemStatus.COOKING) {
            throw new BusinessRuleException(
                    "Cannot cancel item with status: " + item.getStatus()
                            + ". Only NEW or COOKING items can be cancelled");
        }

        if (dto.getReason() == null || dto.getReason().isBlank()) {
            throw new BusinessRuleException("Cancel reason must not be blank");
        }

        item.setStatus(OrderItemStatus.CANCELLED);
        item.setCancelReason(dto.getReason());

        recalculateTotal(item.getOrder());
        recalculateOrderStatus(item.getOrder());
        orderRepository.save(item.getOrder());

        return item;
    }

    @Transactional
    public OrderItem updateItemStatus(Long orderItemId, UpdateItemStatusRequest dto) {
        Optional<OrderItem> itemOpt = orderItemRepository.findById(orderItemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("OrderItem with id " + orderItemId + " not found");
        }
        OrderItem item = itemOpt.get();

        OrderItemStatus current = item.getStatus();
        OrderItemStatus next = dto.getStatus();

        boolean validTransition = false;
        if (current == OrderItemStatus.NEW && next == OrderItemStatus.COOKING) {
            validTransition = true;
        }
        if (current == OrderItemStatus.COOKING && next == OrderItemStatus.READY) {
            validTransition = true;
        }
        if (current == OrderItemStatus.READY && next == OrderItemStatus.SERVED) {
            validTransition = true;
        }

        if (!validTransition) {
            throw new BusinessRuleException(
                    "Invalid status transition: " + current + " → " + next);
        }

        item.setStatus(next);
        orderItemRepository.save(item);

        recalculateOrderStatus(item.getOrder());
        orderRepository.save(item.getOrder());

        return item;
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getKitchenQueue() {
        List<OrderItemStatus> statuses = new ArrayList<>();
        statuses.add(OrderItemStatus.NEW);
        statuses.add(OrderItemStatus.COOKING);
        return orderItemRepository.findByStatusIn(statuses);
    }

    private void recalculateTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getStatus() == OrderItemStatus.CANCELLED) {
                continue;
            }
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        order.setTotalAmount(total);
    }

    private void recalculateOrderStatus(Order order) {
        if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        int activeCount = 0;
        int newCount = 0;
        int cookingCount = 0;
        int readyCount = 0;
        int servedCount = 0;

        for (OrderItem item : order.getItems()) {
            OrderItemStatus s = item.getStatus();
            if (s == OrderItemStatus.CANCELLED) {
                continue;
            }
            activeCount++;
            if (s == OrderItemStatus.NEW) {
                newCount++;
            } else if (s == OrderItemStatus.COOKING) {
                cookingCount++;
            } else if (s == OrderItemStatus.READY) {
                readyCount++;
            } else if (s == OrderItemStatus.SERVED) {
                servedCount++;
            }
        }

        if (activeCount == 0 || newCount == activeCount) {
            order.setStatus(OrderStatus.OPEN);
        } else if (cookingCount > 0) {
            order.setStatus(OrderStatus.IN_PROGRESS);
        } else if (readyCount == activeCount) {
            order.setStatus(OrderStatus.READY);
        } else if (servedCount == activeCount) {
            order.setStatus(OrderStatus.SERVED);
        }
    }
}
