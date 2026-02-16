package com.alessandro.congress_management.services.authenticate;

import com.alessandro.congress_management.dto.authenticate.AuthResponse;
import com.alessandro.congress_management.dto.authenticate.LoginRequest;
import com.alessandro.congress_management.dto.authenticate.RefreshTokenRequest;
import com.alessandro.congress_management.dto.authenticate.RegisterRequest;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.InvalidCredentialsException;
import com.alessandro.congress_management.exceptions.InvalidTokenException;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest) throws DuplicatedEntityException;

    AuthResponse login(LoginRequest loginRequest) throws InvalidCredentialsException;

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) throws InvalidTokenException;

    void logout(String username);


}
