package com.alessandro.congress_management.dto.user_manager;

import lombok.Value;

@Value
public class CreateCongressAdminRequest {
    String email;
    String fullName;
    String phoneNumber;
    String organization;
    String username;
    String password;
    String identificationNumber;
    Long idInstitution;
}
