package ua.edu.duikt.restaurant.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import ua.edu.duikt.restaurant.entity.Employee;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "restaurant-management-jwt-secret-key-change-me-in-production-please";

    private static final long EXPIRATION_MS = 24L * 60 * 60 * 1000;

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(Employee employee) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(employee.getLogin())
                .claim("role", employee.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(KEY)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getLoginFromToken(String token) {
        return validateToken(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return validateToken(token).get("role", String.class);
    }
}
