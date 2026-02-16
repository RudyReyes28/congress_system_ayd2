package com.alessandro.congress_management.services.authenticate;

import com.alessandro.congress_management.dto.authenticate.AuthResponse;
import com.alessandro.congress_management.dto.authenticate.LoginRequest;
import com.alessandro.congress_management.dto.authenticate.RefreshTokenRequest;
import com.alessandro.congress_management.dto.authenticate.RegisterRequest;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.InvalidCredentialsException;
import com.alessandro.congress_management.exceptions.InvalidTokenException;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) throws DuplicatedEntityException {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) throws InvalidCredentialsException {
        return null;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) throws InvalidTokenException {
        return null;
    }

    @Override
    public void logout(String username) {

    }
}
