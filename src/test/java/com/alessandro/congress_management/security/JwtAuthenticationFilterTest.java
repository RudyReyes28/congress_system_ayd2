package com.alessandro.congress_management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.io.IOException;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Limpiar el contexto de seguridad antes de cada test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_withValidToken_shouldAuthenticateUser() throws ServletException, IOException {
        // Arrange
        String bearerToken = "Bearer " + VALID_TOKEN;
        UserDetails userDetails = createUserDetails(TEST_USERNAME);

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertAll(
                () -> assertNotNull(authentication, "La autenticación no debe ser null"),
                () -> assertTrue(authentication.isAuthenticated(), "El usuario debe estar autenticado"),
                () -> assertEquals(TEST_USERNAME, authentication.getName(), "El username debe coincidir"),
                () -> assertEquals(userDetails, authentication.getPrincipal(), "El principal debe ser el UserDetails")
        );

        verify(jwtTokenProvider).validateToken(VALID_TOKEN);
        verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
        verify(userDetailsService).loadUserByUsername(TEST_USERNAME);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withInvalidToken_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        String bearerToken = "Bearer " + INVALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(INVALID_TOKEN)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication, "No debe haber autenticación con token inválido");

        verify(jwtTokenProvider).validateToken(INVALID_TOKEN);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withNoAuthorizationHeader_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication, "No debe haber autenticación sin header Authorization");

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withMalformedAuthorizationHeader_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat " + VALID_TOKEN);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication, "No debe haber autenticación con formato incorrecto");

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withEmptyAuthorizationHeader_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication, "No debe haber autenticación con header vacío");

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withOnlyBearerPrefix_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication, "No debe haber autenticación solo con prefijo Bearer");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_whenUserDetailsServiceThrowsException_shouldContinueFilterChain()
            throws ServletException, IOException {
        // Arrange
        String bearerToken = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME))
                .thenThrow(new RuntimeException("User not found"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication, "No debe haber autenticación cuando falla loadUserByUsername");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_whenTokenValidationThrowsException_shouldContinueFilterChain()
            throws ServletException, IOException {
        // Arrange
        String bearerToken = "Bearer " + VALID_TOKEN;

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(VALID_TOKEN))
                .thenThrow(new RuntimeException("Token validation error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication, "No debe haber autenticación cuando falla la validación del token");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withValidTokenAndAuthorities_shouldSetAuthorities()
            throws ServletException, IOException {
        // Arrange
        String bearerToken = "Bearer " + VALID_TOKEN;
        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .authorities("ROLE_ADMIN_SYSTEM")
                .build();

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertAll(
                () -> assertNotNull(authentication),
                () -> assertEquals(1, authentication.getAuthorities().size()),
                () -> assertTrue(authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN_SYSTEM")))
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_alwaysCallsFilterChain_evenWhenExceptionOccurs()
            throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - El filtro siempre debe continuar la cadena
        verify(filterChain).doFilter(request, response);
    }

    // ------ Helper Methods ------

    private UserDetails createUserDetails(String username) {
        return User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }
}