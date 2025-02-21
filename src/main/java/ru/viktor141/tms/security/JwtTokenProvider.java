package ru.viktor141.tms.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JwtTokenProvider generates and validates JWT tokens.
 * <p>
 * This class provides methods for creating and validating JWT tokens for user authentication.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${JWT_SECRET}")
    private String SECRET_KEY;
    private Key key;
    private static final long EXPIRATION_TIME = 3600000; // 1 hour

    /**
     * Generates a JWT token for the given user details.
     *
     * @param userDetails The user details object.
     * @return A JWT token string.
     */
    public String generateToken(UserDetails userDetails) {
        if (userDetails.getAuthorities().isEmpty()) {
            throw new IllegalArgumentException("User has no roles assigned!");
        }

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("ROLE_USER"))
                .setIssuedAt(new Date()) // Дата выпуска токена
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Срок действия
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Безопасный ключ
                .compact();
    }

    /**
     * Extracts the email from the given JWT token.
     *
     * @param token The JWT token.
     * @return The email of the user.
     * @throws JwtException If the token is invalid or expired.
     */
    public String extractEmail(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates the given JWT token against the user details.
     *
     * @param token        The JWT token.
     * @param userDetails  The user details object.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Checks if the given token has expired.
     *
     * @param token The JWT token.
     * @return True if the token has expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    /**
     * Retrieves the signing key for JWT generation and validation.
     *
     * @return The signing key.
     */
    private Key getSigningKey() {
        if (key == null) {
            byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
            key = Keys.hmacShaKeyFor(keyBytes);
        }
        return key;
    }
}
