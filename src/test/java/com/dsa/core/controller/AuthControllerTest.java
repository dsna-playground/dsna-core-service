package com.dsa.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;

import com.dsa.core.dto.LoginRequest;
import com.dsa.core.model.User;
import com.dsa.core.security.JwtUtils;
import com.dsa.core.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authenticationManager, jwtUtils, userService);
    }

    @Test
    void returnsJwtForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("password");

        User user = new User("alice", "encoded-password", "alice@example.com");
        user.setId(42L);

        when(userService.authenticateUser("alice", "password")).thenReturn(user);
        when(jwtUtils.generateToken(user)).thenReturn("header.payload.signature");

        var response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo("header.payload.signature");
    }

    @Test
    void rejectsInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("wrong-password");

        when(userService.authenticateUser("alice", "wrong-password")).thenReturn(null);

        var response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
