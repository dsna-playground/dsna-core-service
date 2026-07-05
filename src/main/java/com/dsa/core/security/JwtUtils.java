package com.dsa.core.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dsa.core.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private static final int MINIMUM_SECRET_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtils(
            @Value("${jwt.secret:ThisIsASecureKeyThatIsAtLeast256BitsLong}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        
        if (expirationMs <= 0) {
            throw new IllegalArgumentException("JWT expiration must be greater than zero");
        }

        // Ensure we have a secret key of adequate length
        byte[] keyBytes = getValidKeyBytes(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        if (user == null || user.getId() == null || user.getUsername() == null || user.getEmail() == null) {
            throw new IllegalArgumentException("User ID, username, and email are required to generate a JWT");
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("userId", user.getId())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            extractClaims(token);
            return true;
        } catch (JwtException exception) {
            return false;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private byte[] getValidKeyBytes(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be blank");
        }

        // Convert secret to bytes using UTF-8 encoding
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        // If the key is too short, we need to expand it
        if (keyBytes.length < MINIMUM_SECRET_BYTES) {
            // Create a new array with sufficient length
            byte[] expandedKey = new byte[MINIMUM_SECRET_BYTES];
            // Copy the original key bytes
            for (int i = 0; i < expandedKey.length; i++) {
                expandedKey[i] = keyBytes[i % keyBytes.length];
            }
            return expandedKey;
        }
        
        return keyBytes;
    }
}
