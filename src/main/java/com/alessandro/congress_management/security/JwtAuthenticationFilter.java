package com.alessandro.congress_management.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extraer token del header Authorization
            String jwt = getJwtFromRequest(request);

            // Si hay token y es válido
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // Obtener username del token
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // Cargar detalles del usuario
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Crear objeto de autenticación
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Agregar detalles de la petición
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Establecer autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Usuario autenticado: {}", username);
            }
        } catch (Exception ex) {
            log.error("No se pudo establecer autenticación de usuario: {}", ex.getMessage());
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }


    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Verificar que el header existe y tiene el formato correcto
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remover "Bearer " prefix
        }

        return null;
    }
}