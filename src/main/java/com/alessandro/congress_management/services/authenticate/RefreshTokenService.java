package com.alessandro.congress_management.services.authenticate;

import com.alessandro.congress_management.exceptions.InvalidTokenException;
import com.alessandro.congress_management.models.authentication_and_users.RefreshTokenEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;

public interface RefreshTokenService {
    RefreshTokenEntity createRefreshToken(UserEntity user);

    RefreshTokenEntity validateRefreshToken(String token) throws InvalidTokenException;

    void deleteByUser(UserEntity user);

    void deleteExpiredTokens();
}
