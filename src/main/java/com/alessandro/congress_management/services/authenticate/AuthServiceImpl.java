package com.alessandro.congress_management.services.authenticate;

import com.alessandro.congress_management.dto.authenticate.*;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.InvalidCredentialsException;
import com.alessandro.congress_management.exceptions.InvalidTokenException;
import com.alessandro.congress_management.models.authentication_and_users.RefreshTokenEntity;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.RoleRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService{
    private final String DEFAULT_ROLE = "PARTICIPANT";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthServiceImpl(
            UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) throws DuplicatedEntityException {
        // Validar unicidad de username
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicatedEntityException("El username ya está en uso");
        }

        // Validar unicidad de email
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicatedEntityException("El email ya está registrado");
        }

        // Validar unicidad de número de identificación
        if (userRepository.existsByIdentificationNumber(registerRequest.getIdentificationNumber())) {
            throw new DuplicatedEntityException("El número de identificación ya está registrado");
        }

        RoleEntity defaultRole = roleRepository.findByRoleName(DEFAULT_ROLE)
                .orElseThrow(() -> new DuplicatedEntityException("El rol especificado no existe"));

        // Hashear password
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // Crear entidad
        UserEntity user = registerRequest.createEntity(hashedPassword, defaultRole);

        // Guardar usuario
        UserEntity savedUser = userRepository.save(user);



        //log.info("Usuario registrado exitosamente: {}", savedUser.getUsername());

        // Generar tokens
        return generateAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) throws InvalidCredentialsException {
        // Buscar usuario
        UserEntity user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        // Validar que el usuario esté activo
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Usuario inactivo");
        }

        // Validar password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        log.info("Usuario autenticado exitosamente: {}", user.getUsername());

        // Generar tokens
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) throws InvalidTokenException {
        // Validar refresh token
        RefreshTokenEntity refreshToken = refreshTokenService.validateRefreshToken(
                refreshTokenRequest.getRefreshToken());

        UserEntity user = refreshToken.getUser();

        // Validar que el usuario esté activo
        if (!user.getIsActive()) {
            throw new InvalidTokenException("Usuario inactivo");
        }

        // Generar nuevo access token
        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getIdUser());

        log.info("Token refrescado para usuario: {}", user.getUsername());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getExpirationMs() / 1000,
                UserInfoResponse.fromEntity(user)
        );
    }

    @Override
    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    refreshTokenService.deleteByUser(user);
                    log.info("Usuario deslogueado: {}", username);
                });
    }

    private AuthResponse generateAuthResponse(UserEntity user) {
        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getIdUser());
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getExpirationMs() / 1000,
                UserInfoResponse.fromEntity(user)
        );
    }
}
