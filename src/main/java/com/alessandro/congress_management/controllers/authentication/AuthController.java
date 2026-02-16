package com.alessandro.congress_management.controllers.authentication;

import com.alessandro.congress_management.dto.authenticate.*;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.InvalidCredentialsException;
import com.alessandro.congress_management.exceptions.InvalidTokenException;
import com.alessandro.congress_management.services.authenticate.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409",
                    description = "Username, email o identificación ya registrados")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest)
            throws DuplicatedEntityException {

        //log.info("Petición de registro para usuario: {}", registerRequest.getUsername());

        AuthResponse response = authService.register(registerRequest);

        //log.info("Usuario registrado exitosamente: {}", registerRequest.getUsername());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Autentica un usuario y retorna tokens de acceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401",
                    description = "Credenciales inválidas o usuario inactivo")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest)
            throws InvalidCredentialsException {

        //log.info("Intento de login para usuario: {}", loginRequest.getUsername());

        AuthResponse response = authService.login(loginRequest);

        //log.info("Login exitoso para usuario: {}", loginRequest.getUsername());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh-token")
    @Operation(summary = "Refrescar token de acceso",
            description = "Obtiene un nuevo access token usando un refresh token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Token refrescado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401",
                    description = "Refresh token inválido o expirado")
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest)
            throws InvalidTokenException {

        //log.info("Petición de refresh token");

        AuthResponse response = authService.refreshToken(refreshTokenRequest);

        //log.info("Token refrescado exitosamente");

        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión",
            description = "Invalida los refresh tokens del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Sesión cerrada exitosamente"),
            @ApiResponse(responseCode = "400",
                    description = "Datos de entrada inválidos")
    })
    public ResponseEntity<MessageResponse> logout(@RequestParam String username) {

        //log.info("Petición de logout para usuario: {}", username);

        authService.logout(username);

        //log.info("Logout exitoso para usuario: {}", username);

        return ResponseEntity.ok(
                new MessageResponse("Sesión cerrada exitosamente")
        );
    }
}
