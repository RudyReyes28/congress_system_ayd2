package com.alessandro.congress_management.security;

import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "$2a$10$hashedpassword";

    @Test
    void testLoadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        // Arrange
        UserEntity user = createActiveUser(TEST_USERNAME, "ADMIN_SYSTEM");

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertAll(
                () -> assertNotNull(userDetails, "UserDetails no debe ser null"),
                () -> assertEquals(TEST_USERNAME, userDetails.getUsername(), "Username debe coincidir"),
                () -> assertEquals(TEST_PASSWORD, userDetails.getPassword(), "Password debe coincidir"),
                () -> assertTrue(userDetails.isEnabled(), "Usuario debe estar habilitado"),
                () -> assertTrue(userDetails.isAccountNonLocked(), "Cuenta no debe estar bloqueada"),
                () -> assertTrue(userDetails.isAccountNonExpired(), "Cuenta no debe estar expirada"),
                () -> assertTrue(userDetails.isCredentialsNonExpired(), "Credenciales no deben estar expiradas")
        );

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void testLoadUserByUsername_whenUserNotFound_shouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(TEST_USERNAME)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        assertTrue(exception.getMessage().contains(TEST_USERNAME));

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void testLoadUserByUsername_whenUserIsInactive_shouldThrowException() {
        // Arrange
        UserEntity inactiveUser = createInactiveUser(TEST_USERNAME);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(TEST_USERNAME)
        );

        assertTrue(exception.getMessage().contains("Usuario inactivo"));
        assertTrue(exception.getMessage().contains(TEST_USERNAME));

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void testLoadUserByUsername_withRoleAdminSystem_shouldAddRolePrefix() {
        // Arrange
        UserEntity user = createActiveUser(TEST_USERNAME, "ADMIN_SYSTEM");

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertAll(
                () -> assertEquals(1, userDetails.getAuthorities().size(), "Debe tener 1 autoridad"),
                () -> assertTrue(
                        hasAuthority(userDetails, "ROLE_ADMIN_SYSTEM"),
                        "Debe tener el rol ROLE_ADMIN_SYSTEM"
                )
        );

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void testLoadUserByUsername_withRoleAdminCongress_shouldAddRolePrefix() {
        // Arrange
        UserEntity user = createActiveUser(TEST_USERNAME, "ADMIN_CONGRESS");

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertTrue(
                hasAuthority(userDetails, "ROLE_ADMIN_CONGRESS"),
                "Debe tener el rol ROLE_ADMIN_CONGRESS"
        );
    }

    @Test
    void testLoadUserByUsername_withRoleParticipant_shouldAddRolePrefix() {
        // Arrange
        UserEntity user = createActiveUser(TEST_USERNAME, "PARTICIPANT");

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertTrue(
                hasAuthority(userDetails, "ROLE_PARTICIPANT"),
                "Debe tener el rol ROLE_PARTICIPANT"
        );
    }

    @Test
    void testLoadUserByUsername_withRoleAlreadyHavingPrefix_shouldNotDuplicatePrefix() {
        // Arrange
        UserEntity user = createActiveUser(TEST_USERNAME, "ROLE_ADMIN_SYSTEM");

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        long roleCount = userDetails.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().equals("ROLE_ADMIN_SYSTEM"))
                .count();

        assertAll(
                () -> assertEquals(1, userDetails.getAuthorities().size(),
                        "Solo debe tener 1 autoridad"),
                () -> assertEquals(1, roleCount,
                        "No debe duplicar el prefijo ROLE_")
        );
    }

    @Test
    void testLoadUserByUsername_withNullRole_shouldHaveNoAuthorities() {
        // Arrange
        UserEntity user = createActiveUser(TEST_USERNAME, null);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertTrue(
                userDetails.getAuthorities().isEmpty(),
                "No debe tener autoridades si el rol es null"
        );

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void testLoadUserByUsername_shouldSetAccountLockedBasedOnIsActive() {
        // Arrange
        UserEntity user = createActiveUser(TEST_USERNAME, "PARTICIPANT");
        user.setIsActive(false);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(TEST_USERNAME),
                "Debe lanzar excepción para usuario inactivo"
        );
    }

    @Test
    void testLoadUserByUsername_multipleCallsWithSameUsername_shouldCallRepository() {
        // Arrange
        UserEntity user = createActiveUser(TEST_USERNAME, "ADMIN_SYSTEM");

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        customUserDetailsService.loadUserByUsername(TEST_USERNAME);
        customUserDetailsService.loadUserByUsername(TEST_USERNAME);
        customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        verify(userRepository, times(3)).findByUsername(TEST_USERNAME);
    }

    @Test
    void testLoadUserByUsername_withDifferentUsernames_shouldLoadDifferentUsers() {
        // Arrange
        UserEntity user1 = createActiveUser("user1", "ADMIN_SYSTEM");
        UserEntity user2 = createActiveUser("user2", "PARTICIPANT");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));

        // Act
        UserDetails userDetails1 = customUserDetailsService.loadUserByUsername("user1");
        UserDetails userDetails2 = customUserDetailsService.loadUserByUsername("user2");

        // Assert
        assertAll(
                () -> assertEquals("user1", userDetails1.getUsername()),
                () -> assertEquals("user2", userDetails2.getUsername()),
                () -> assertTrue(hasAuthority(userDetails1, "ROLE_ADMIN_SYSTEM")),
                () -> assertTrue(hasAuthority(userDetails2, "ROLE_PARTICIPANT"))
        );

        verify(userRepository).findByUsername("user1");
        verify(userRepository).findByUsername("user2");
    }

    @Test
    void testLoadUserByUsername_caseSensitiveUsername() {
        // Arrange
        UserEntity user = createActiveUser("TestUser", "ADMIN_SYSTEM");

        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("TestUser");

        // Assert
        assertEquals("TestUser", userDetails.getUsername());

        // Verify que el username es case-sensitive
        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("testuser")
        );
    }

    // ===== Helper Methods =====

    private UserEntity createActiveUser(String username, String roleName) {
        UserEntity user = new UserEntity();
        user.setIdUser(1L);
        user.setUsername(username);
        user.setPassword(TEST_PASSWORD);
        user.setEmail(username + "@test.com");
        user.setFullName("Test User");
        user.setIdentificationNumber("12345678");
        user.setPhoneNumber("1234-5678");
        user.setOrganization("Test Org");
        user.setIsActive(true);

        if (roleName != null) {
            RoleEntity role = new RoleEntity();
            role.setIdRole(1);
            role.setRoleName(roleName);
            role.setDescription("Test role");
            user.setRole(role);
        }

        return user;
    }

    private UserEntity createInactiveUser(String username) {
        UserEntity user = createActiveUser(username, "PARTICIPANT");
        user.setIsActive(false);
        return user;
    }

    private boolean hasAuthority(UserDetails userDetails, String authority) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(authority));
    }
}
