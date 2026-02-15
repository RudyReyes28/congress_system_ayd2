package com.alessandro.congress_management.dto.authenticate;

import lombok.Value;

@Value
public class LoginRequest {
    String username;
    String password;
}
