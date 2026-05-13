package ua.edu.duikt.restaurant.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    protected static final String WAITER_LOGIN = "waiter";
    protected static final String WAITER_PASSWORD = "waiter123";
    protected static final String COOK_LOGIN = "cook";
    protected static final String COOK_PASSWORD = "cook123";
    protected static final String CASHIER_LOGIN = "cashier";
    protected static final String CASHIER_PASSWORD = "cashier123";
    protected static final String ADMIN_LOGIN = "admin";
    protected static final String ADMIN_PASSWORD = "admin123";

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("restaurant_db")
            .withUsername("restaurant_user")
            .withPassword("restaurant_pass");

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String loginAndGetToken(String login, String password) {
        Map<String, String> body = Map.of("login", login, "password", password);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", body, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Login failed for " + login + ": " + response.getStatusCode());
        }
        return readTree(response.getBody()).get("token").asText();
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    protected ResponseEntity<String> exchange(HttpMethod method, String path, String token, Object body) {
        HttpEntity<Object> entity = new HttpEntity<>(body, authHeaders(token));
        return restTemplate.exchange(path, method, entity, String.class);
    }

    protected JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse JSON: " + json, e);
        }
    }
}
