package ua.edu.duikt.restaurant.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import ua.edu.duikt.restaurant.entity.Employee;
import ua.edu.duikt.restaurant.entity.MenuItem;
import ua.edu.duikt.restaurant.entity.RestaurantTable;
import ua.edu.duikt.restaurant.entity.enums.TableStatus;
import ua.edu.duikt.restaurant.repository.EmployeeRepository;
import ua.edu.duikt.restaurant.repository.MenuItemRepository;
import ua.edu.duikt.restaurant.repository.RestaurantTableRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class OrderFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RestaurantTableRepository tableRepository;
    @Autowired
    private MenuItemRepository menuItemRepository;

    private static Long waiterId;
    private static Long cashierId;
    private static Long tableId;
    private static int tableNumber;
    private static Long menuItemId;

    private static Long orderId;
    private static Long itemId1;
    private static Long itemId3;

    @BeforeAll
    void setUpIds() {
        Employee waiter = employeeRepository.findByLogin(WAITER_LOGIN).orElseThrow();
        Employee cashier = employeeRepository.findByLogin(CASHIER_LOGIN).orElseThrow();
        RestaurantTable freeTable = tableRepository.findByStatus(TableStatus.FREE).get(0);
        MenuItem availableItem = menuItemRepository.findByIsAvailableTrue().get(0);

        waiterId = waiter.getId();
        cashierId = cashier.getId();
        tableId = freeTable.getId();
        tableNumber = freeTable.getNumber();
        menuItemId = availableItem.getId();
    }

    @Test
    @Order(1)
    void us01_openOrder_succeeds_whenTableIsFree() {
        String token = loginAndGetToken(WAITER_LOGIN, WAITER_PASSWORD);
        Map<String, Long> body = Map.of("tableId", tableId, "waiterId", waiterId);

        ResponseEntity<String> response = exchange(HttpMethod.POST, "/api/orders", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        JsonNode json = readTree(response.getBody());
        assertThat(json.get("status").asText()).isEqualTo("OPEN");
        orderId = json.get("id").asLong();

        ResponseEntity<String> freeTablesResp =
                exchange(HttpMethod.GET, "/api/tables/free", token, null);
        assertThat(freeTablesResp.getStatusCode().value()).isEqualTo(200);
        for (JsonNode t : readTree(freeTablesResp.getBody())) {
            assertThat(t.get("number").asInt()).isNotEqualTo(tableNumber);
        }
    }

    @Test
    @Order(2)
    void us01_openOrder_returns409_whenTableOccupied() {
        String token = loginAndGetToken(WAITER_LOGIN, WAITER_PASSWORD);
        Map<String, Long> body = Map.of("tableId", tableId, "waiterId", waiterId);

        ResponseEntity<String> response = exchange(HttpMethod.POST, "/api/orders", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @Order(3)
    void us01_openOrder_returns403_whenRoleIsCook() {
        String token = loginAndGetToken(COOK_LOGIN, COOK_PASSWORD);
        Map<String, Long> body = Map.of("tableId", tableId, "waiterId", waiterId);

        ResponseEntity<String> response = exchange(HttpMethod.POST, "/api/orders", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    @Order(4)
    void us02_addItem_succeeds_whenItemIsAvailable() {
        String token = loginAndGetToken(WAITER_LOGIN, WAITER_PASSWORD);
        Map<String, Object> body = Map.of("menuItemId", menuItemId, "quantity", 2);

        ResponseEntity<String> response = exchange(
                HttpMethod.POST, "/api/orders/" + orderId + "/items", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        JsonNode json = readTree(response.getBody());
        assertThat(json.get("status").asText()).isEqualTo("NEW");
        itemId1 = json.get("id").asLong();
    }

    @Test
    @Order(5)
    void us02_addItem_returns404_whenOrderMissing() {
        String token = loginAndGetToken(WAITER_LOGIN, WAITER_PASSWORD);
        Map<String, Object> body = Map.of("menuItemId", menuItemId, "quantity", 1);

        ResponseEntity<String> response = exchange(
                HttpMethod.POST, "/api/orders/999999/items", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @Order(6)
    void us03_updateItemStatus_succeeds_NewToCooking() {
        String token = loginAndGetToken(COOK_LOGIN, COOK_PASSWORD);
        Map<String, Object> body = Map.of("status", "COOKING");

        ResponseEntity<String> response = exchange(
                HttpMethod.PATCH, "/api/orders/items/" + itemId1 + "/status", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(readTree(response.getBody()).get("status").asText()).isEqualTo("COOKING");
    }

    @Test
    @Order(7)
    void us03_updateItemStatus_returns409_whenSkippingReady() {
        String token = loginAndGetToken(COOK_LOGIN, COOK_PASSWORD);
        Map<String, Object> body = Map.of("status", "SERVED");

        ResponseEntity<String> response = exchange(
                HttpMethod.PATCH, "/api/orders/items/" + itemId1 + "/status", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @Order(8)
    void us04_cancelItem_succeeds_withReason() {
        String waiterToken = loginAndGetToken(WAITER_LOGIN, WAITER_PASSWORD);

        Map<String, Object> addBody = Map.of("menuItemId", menuItemId, "quantity", 1);
        ResponseEntity<String> addResp = exchange(
                HttpMethod.POST, "/api/orders/" + orderId + "/items", waiterToken, addBody);
        assertThat(addResp.getStatusCode().value()).isEqualTo(201);
        Long itemId2 = readTree(addResp.getBody()).get("id").asLong();

        Map<String, String> cancelBody = Map.of("reason", "Гість передумав");
        ResponseEntity<String> cancelResp = exchange(
                HttpMethod.DELETE, "/api/orders/items/" + itemId2, waiterToken, cancelBody);

        assertThat(cancelResp.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    @Order(9)
    void us04_cancelItem_returns400_whenReasonBlank() {
        String waiterToken = loginAndGetToken(WAITER_LOGIN, WAITER_PASSWORD);

        Map<String, Object> addBody = Map.of("menuItemId", menuItemId, "quantity", 1);
        ResponseEntity<String> addResp = exchange(
                HttpMethod.POST, "/api/orders/" + orderId + "/items", waiterToken, addBody);
        assertThat(addResp.getStatusCode().value()).isEqualTo(201);
        itemId3 = readTree(addResp.getBody()).get("id").asLong();

        Map<String, String> cancelBody = Map.of("reason", "   ");
        ResponseEntity<String> cancelResp = exchange(
                HttpMethod.DELETE, "/api/orders/items/" + itemId3, waiterToken, cancelBody);

        assertThat(cancelResp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @Order(10)
    void us06_createMenuItem_succeeds_asAdmin() {
        String token = loginAndGetToken(ADMIN_LOGIN, ADMIN_PASSWORD);
        Map<String, Object> body = Map.of(
                "name", "Тестова страва",
                "description", "Інтеграційний тест",
                "price", 150.00,
                "categoryId", 1);

        ResponseEntity<String> response = exchange(
                HttpMethod.POST, "/api/menu/items", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(readTree(response.getBody()).get("name").asText()).isEqualTo("Тестова страва");
    }

    @Test
    @Order(11)
    void us06_createMenuItem_returns403_asWaiter() {
        String token = loginAndGetToken(WAITER_LOGIN, WAITER_PASSWORD);
        Map<String, Object> body = Map.of(
                "name", "Заборонено",
                "price", 99.00,
                "categoryId", 1);

        ResponseEntity<String> response = exchange(
                HttpMethod.POST, "/api/menu/items", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    @Order(12)
    void us05_payment_returns409_whenItemNotServed() {
        String token = loginAndGetToken(CASHIER_LOGIN, CASHIER_PASSWORD);
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "method", "CASH",
                "cashierId", cashierId);

        ResponseEntity<String> response = exchange(HttpMethod.POST, "/api/payments", token, body);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @Order(13)
    void us05_payment_succeeds_whenAllServed() {
        String cookToken = loginAndGetToken(COOK_LOGIN, COOK_PASSWORD);
        progressItemToServed(itemId1, "COOKING", cookToken);
        progressItemToServed(itemId3, "NEW", cookToken);

        String cashierToken = loginAndGetToken(CASHIER_LOGIN, CASHIER_PASSWORD);
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "method", "CASH",
                "cashierId", cashierId);

        ResponseEntity<String> payResp =
                exchange(HttpMethod.POST, "/api/payments", cashierToken, body);

        assertThat(payResp.getStatusCode().value()).isEqualTo(201);
        JsonNode payment = readTree(payResp.getBody());
        assertThat(payment.get("orderId").asLong()).isEqualTo(orderId);

        ResponseEntity<String> tablesResp =
                exchange(HttpMethod.GET, "/api/tables/free", cashierToken, null);
        boolean tableIsFreeAgain = false;
        for (JsonNode t : readTree(tablesResp.getBody())) {
            if (t.get("number").asInt() == tableNumber) {
                tableIsFreeAgain = true;
                break;
            }
        }
        assertThat(tableIsFreeAgain).isTrue();
    }

    private void progressItemToServed(Long itemId, String fromStatus, String cookToken) {
        if ("NEW".equals(fromStatus)) {
            patchStatus(itemId, "COOKING", cookToken);
            patchStatus(itemId, "READY", cookToken);
            patchStatus(itemId, "SERVED", cookToken);
        } else if ("COOKING".equals(fromStatus)) {
            patchStatus(itemId, "READY", cookToken);
            patchStatus(itemId, "SERVED", cookToken);
        }
    }

    private void patchStatus(Long itemId, String status, String token) {
        ResponseEntity<String> resp = exchange(
                HttpMethod.PATCH,
                "/api/orders/items/" + itemId + "/status",
                token,
                Map.of("status", status));
        assertThat(resp.getStatusCode().is2xxSuccessful())
                .as("PATCH status %s on item %d", status, itemId)
                .isTrue();
    }
}
