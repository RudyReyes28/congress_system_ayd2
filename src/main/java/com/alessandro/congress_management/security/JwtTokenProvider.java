package com.alessandro.congress_management.security;

public class JwtTokenProvider {

    public JwtTokenProvider(String testSecret, long testExpirationMs) {
    }

    public String generateToken(String username) {

        return null;
    }

    public boolean validateToken(String token) {

        return token.startsWith("generated jwt token for");
    }

    public String getUsernameFromToken(String token) {

        return null;
    }

    public long getExpirationMs() {

        return 3600L;
    }
}
