package com.alessandro.congress_management.services.authenticate;

import com.alessandro.congress_management.exceptions.InvalidTokenException;
import com.alessandro.congress_management.models.authentication_and_users.RefreshTokenEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDurationMs;

    @Autowired
    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenDurationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }

    @Override
    @Transactional
    public RefreshTokenEntity createRefreshToken(UserEntity user) {
        return null;
    }

    @Override
    public RefreshTokenEntity validateRefreshToken(String token) throws InvalidTokenException {
        return null;
    }

    @Override
    public void deleteByUser(UserEntity user) {

    }

    @Override
    public void deleteExpiredTokens() {

    }

}
