package ua.edu.duikt.restaurant.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.edu.duikt.restaurant.dto.request.CreatePaymentRequest;
import ua.edu.duikt.restaurant.entity.Employee;
import ua.edu.duikt.restaurant.entity.Order;
import ua.edu.duikt.restaurant.entity.OrderItem;
import ua.edu.duikt.restaurant.entity.Payment;
import ua.edu.duikt.restaurant.entity.RestaurantTable;
import ua.edu.duikt.restaurant.entity.enums.EmployeeRole;
import ua.edu.duikt.restaurant.entity.enums.OrderItemStatus;
import ua.edu.duikt.restaurant.entity.enums.OrderStatus;
import ua.edu.duikt.restaurant.entity.enums.PaymentMethod;
import ua.edu.duikt.restaurant.entity.enums.TableStatus;
import ua.edu.duikt.restaurant.exception.BusinessRuleException;
import ua.edu.duikt.restaurant.exception.NotFoundException;
import ua.edu.duikt.restaurant.repository.EmployeeRepository;
import ua.edu.duikt.restaurant.repository.OrderRepository;
import ua.edu.duikt.restaurant.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void closeOrderAndPay_shouldCreatePayment_whenAllItemsServed() {
        RestaurantTable table = buildTable(1L);
        Order order = buildOrder(1L, OrderStatus.SERVED, BigDecimal.valueOf(178), table);
        order.getItems().add(buildItem(OrderItemStatus.SERVED, order));
        order.getItems().add(buildItem(OrderItemStatus.SERVED, order));
        Employee cashier = buildCashier(1L);
        CreatePaymentRequest request = buildRequest(1L, 1L, PaymentMethod.CASH);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.closeOrderAndPay(request);

        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(178));
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CLOSED);
        assertThat(order.getClosedAt()).isNotNull();
        assertThat(table.getStatus()).isEqualTo(TableStatus.FREE);
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(order);
    }

    @Test
    void closeOrderAndPay_shouldSucceed_whenAllItemsServedOrCancelled() {
        RestaurantTable table = buildTable(1L);
        Order order = buildOrder(1L, OrderStatus.SERVED, BigDecimal.valueOf(89), table);
        order.getItems().add(buildItem(OrderItemStatus.SERVED, order));
        order.getItems().add(buildItem(OrderItemStatus.CANCELLED, order));
        Employee cashier = buildCashier(1L);
        CreatePaymentRequest request = buildRequest(1L, 1L, PaymentMethod.CARD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.closeOrderAndPay(request);

        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CLOSED);
    }

    @Test
    void closeOrderAndPay_shouldThrow_whenOrderAlreadyClosed() {
        Order order = buildOrder(1L, OrderStatus.CLOSED, BigDecimal.valueOf(100), buildTable(1L));
        CreatePaymentRequest request = buildRequest(1L, 1L, PaymentMethod.CASH);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.closeOrderAndPay(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already closed");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void closeOrderAndPay_shouldThrow_whenNotAllItemsServed() {
        RestaurantTable table = buildTable(1L);
        Order order = buildOrder(1L, OrderStatus.IN_PROGRESS, BigDecimal.valueOf(178), table);
        order.getItems().add(buildItem(OrderItemStatus.SERVED, order));
        order.getItems().add(buildItem(OrderItemStatus.COOKING, order));
        CreatePaymentRequest request = buildRequest(1L, 1L, PaymentMethod.CASH);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.closeOrderAndPay(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Not all items are served");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void closeOrderAndPay_shouldThrow_whenOrderNotFound() {
        CreatePaymentRequest request = buildRequest(99L, 1L, PaymentMethod.CASH);

        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.closeOrderAndPay(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    private RestaurantTable buildTable(Long id) {
        RestaurantTable table = new RestaurantTable();
        table.setId(id);
        table.setNumber(id.intValue());
        table.setCapacity(4);
        table.setStatus(TableStatus.OCCUPIED);
        return table;
    }

    private Order buildOrder(Long id, OrderStatus status, BigDecimal total, RestaurantTable table) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setTotalAmount(total);
        order.setTable(table);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private OrderItem buildItem(OrderItemStatus status, Order order) {
        OrderItem item = new OrderItem();
        item.setStatus(status);
        item.setOrder(order);
        item.setUnitPrice(BigDecimal.valueOf(89));
        item.setQuantity(1);
        return item;
    }

    private Employee buildCashier(Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFullName("Олена Сидоренко");
        employee.setLogin("o.sydorenko");
        employee.setPasswordHash("x");
        employee.setRole(EmployeeRole.CASHIER);
        employee.setActive(true);
        return employee;
    }

    private CreatePaymentRequest buildRequest(Long orderId, Long cashierId, PaymentMethod method) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(orderId);
        request.setCashierId(cashierId);
        request.setMethod(method);
        return request;
    }
}
