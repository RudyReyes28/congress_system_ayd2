package com.alessandro.congress_management.dto.user_manager;

import lombok.Value;

@Value
public class UpdateUserRequest {
    String username;
    String email;
    String fullName;
    String phoneNumber;
    String organization;
    String identificationNumber;

}
