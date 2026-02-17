package com.alessandro.congress_management.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Autowired
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                // Habilitar CORS con la configuración definida en CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))


                // Deshabilitar CSRF
                .csrf(csrf -> csrf.disable())


                // Configurar sesiones como STATELESS (sin estado)
                // No se crean sesiones HTTP, todo se maneja con JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                // Configurar qué rutas son publicas y cuáles requieren autenticación
                .authorizeHttpRequests(auth -> auth

                        // Endpoints de autenticación
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh-token"
                        ).permitAll()

                        // Swagger UI y OpenAPI docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()


                        // Logout requiere autenticación
                        .requestMatchers("/api/v1/auth/logout").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )


                // Agregar filtro JWT antes del filtro de autenticación de usuario/contraseña
                // Esto permite que el JWT sea validado antes de intentar autenticar
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}