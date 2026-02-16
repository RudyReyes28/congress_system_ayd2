package com.alessandro.congress_management.services.authenticate;

import com.alessandro.congress_management.exceptions.InvalidTokenException;
import com.alessandro.congress_management.models.authentication_and_users.PersonEntity;
import com.alessandro.congress_management.models.authentication_and_users.RefreshTokenEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {
    private static final long TEST_REFRESH_EXPIRATION_MS = 604800000L; // 7 días

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService =
                new RefreshTokenServiceImpl(refreshTokenRepository, TEST_REFRESH_EXPIRATION_MS);
    }

    @Test
    void testCreateRefreshToken() {
        // Arrange
        UserEntity user = createTestUser();
        ArgumentCaptor<RefreshTokenEntity> tokenCaptor = ArgumentCaptor.forClass(RefreshTokenEntity.class);

        when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshTokenEntity result = refreshTokenService.createRefreshToken(user);

        // Assert
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshTokenEntity captured = tokenCaptor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(user, captured.getUser()),
                () -> assertNotNull(captured.getToken()),
                () -> assertNotNull(captured.getExpiryDate()),
                () -> assertTrue(captured.getExpiryDate().isAfter(LocalDateTime.now()))
        );
    }

    @Test
    void testValidateRefreshToken_whenValid() throws InvalidTokenException {
        // Arrange
        String tokenString = "valid-refresh-token";
        RefreshTokenEntity token = createValidRefreshToken();

        when(refreshTokenRepository.findByToken(tokenString))
                .thenReturn(Optional.of(token));

        // Act
        RefreshTokenEntity result = refreshTokenService.validateRefreshToken(tokenString);

        // Assert
        assertNotNull(result);
        assertEquals(token, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void testValidateRefreshToken_whenNotFound() {
        // Arrange
        String tokenString = "non-existent-token";

        when(refreshTokenRepository.findByToken(tokenString))
                .thenReturn(Optional.empty());

        // Assert
        assertThrows(InvalidTokenException.class,
                () -> refreshTokenService.validateRefreshToken(tokenString));
    }

    @Test
    void testValidateRefreshToken_whenExpired() {
        // Arrange
        String tokenString = "expired-token";
        RefreshTokenEntity expiredToken = createExpiredRefreshToken();

        when(refreshTokenRepository.findByToken(tokenString))
                .thenReturn(Optional.of(expiredToken));

        // Assert
        assertThrows(InvalidTokenException.class,
                () -> refreshTokenService.validateRefreshToken(tokenString));

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void testDeleteByUser() {
        // Arrange
        UserEntity user = createTestUser();

        // Act
        refreshTokenService.deleteByUser(user);

        // Assert
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void testDeleteExpiredTokens() {
        // Act
        refreshTokenService.deleteExpiredTokens();

        // Assert
        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    // Helper methods

    private UserEntity createTestUser() {
        UserEntity user = new UserEntity();
        user.setIdUser(1L);
        user.setPerson(new PersonEntity());
        user.setUsername("testuser");
        user.setIdentificationNumber("12345678");
        user.setIsActive(true);
        return user;
    }

    private RefreshTokenEntity createValidRefreshToken() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setIdToken(1L);
        token.setToken("valid-token");
        token.setUser(createTestUser());
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        return token;
    }

    private RefreshTokenEntity createExpiredRefreshToken() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setIdToken(2L);
        token.setToken("expired-token");
        token.setUser(createTestUser());
        token.setExpiryDate(LocalDateTime.now().minusDays(1));
        return token;
    }

}
