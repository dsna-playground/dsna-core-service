package com.dsa.core.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dsa.core.model.User;

import io.jsonwebtoken.Claims;

class JwtUtilsTest {

    private static final String TEST_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(TEST_SECRET, 60_000);
    }

    @Test
    void generatesAndValidatesTokenWithUserClaims() {
        User user = new User("alice", "unused-password", "alice@example.com");
        user.setId(42L);

        String token = jwtUtils.generateToken(user);
        Claims claims = jwtUtils.extractClaims(token);

        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.extractUsername(token)).isEqualTo("alice");
        assertThat(claims.get("username", String.class)).isEqualTo("alice");
        assertThat(claims.get("email", String.class)).isEqualTo("alice@example.com");
        assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(42L);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rejectsTamperedToken() {
        User user = new User("alice", "unused-password", "alice@example.com");
        user.setId(42L);

        String token = jwtUtils.generateToken(user);

        assertThat(jwtUtils.validateToken(token + "tampered")).isFalse();
    }
}
