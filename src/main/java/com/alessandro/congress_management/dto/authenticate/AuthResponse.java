package com.alessandro.congress_management.dto.authenticate;

import lombok.Value;

@Value
public class AuthResponse {
    String accessToken;
    String refreshToken;
    String tokenType = "Bearer";
    Long expiresIn;
    UserInfoResponse user;

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserInfoResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

}
