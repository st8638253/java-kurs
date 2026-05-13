package ua.edu.duikt.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.duikt.restaurant.dto.request.CreatePaymentRequest;
import ua.edu.duikt.restaurant.entity.Employee;
import ua.edu.duikt.restaurant.entity.Order;
import ua.edu.duikt.restaurant.entity.OrderItem;
import ua.edu.duikt.restaurant.entity.Payment;
import ua.edu.duikt.restaurant.entity.enums.OrderItemStatus;
import ua.edu.duikt.restaurant.entity.enums.OrderStatus;
import ua.edu.duikt.restaurant.entity.enums.TableStatus;
import ua.edu.duikt.restaurant.exception.BusinessRuleException;
import ua.edu.duikt.restaurant.exception.NotFoundException;
import ua.edu.duikt.restaurant.repository.EmployeeRepository;
import ua.edu.duikt.restaurant.repository.OrderRepository;
import ua.edu.duikt.restaurant.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Payment closeOrderAndPay(CreatePaymentRequest dto) {
        Optional<Order> orderOpt = orderRepository.findById(dto.getOrderId());
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order with id " + dto.getOrderId() + " not found");
        }
        Order order = orderOpt.get();

        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new BusinessRuleException("Order " + order.getId() + " is already closed");
        }

        boolean hasUnservedItems = false;
        for (OrderItem item : order.getItems()) {
            if (item.getStatus() != OrderItemStatus.SERVED && item.getStatus() != OrderItemStatus.CANCELLED) {
                hasUnservedItems = true;
                break;
            }
        }

        if (hasUnservedItems) {
            throw new BusinessRuleException(
                    "Not all items are served. Close the order only after all items are SERVED or CANCELLED");
        }

        Optional<Employee> cashierOpt = employeeRepository.findById(dto.getCashierId());
        if (cashierOpt.isEmpty()) {
            throw new NotFoundException("Employee with id " + dto.getCashierId() + " not found");
        }
        Employee cashier = cashierOpt.get();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setCashier(cashier);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(dto.getMethod());
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);

        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(LocalDateTime.now());
        order.getTable().setStatus(TableStatus.FREE);
        orderRepository.save(order);

        return payment;
    }
}
