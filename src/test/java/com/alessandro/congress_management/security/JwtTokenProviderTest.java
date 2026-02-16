package com.alessandro.congress_management.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {
    private static final String TEST_SECRET = "testSecretKeyForJwtTokenProviderMustBeAtLeast256BitsLongForHS512Algorithm";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hora
    private static final String TEST_USERNAME = "testuser";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION_MS);
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, 1L);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetUsernameFromToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, 1L);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void testValidateToken_whenValid() {
        // Arrange
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, 1L);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_whenInvalid() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_whenEmpty() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGetExpirationMs() {
        // Act
        long expirationMs = jwtTokenProvider.getExpirationMs();

        // Assert
        assertEquals(TEST_EXPIRATION_MS, expirationMs);
    }
}
