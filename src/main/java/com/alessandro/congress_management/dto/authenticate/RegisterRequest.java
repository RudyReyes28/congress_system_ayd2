package com.alessandro.congress_management.dto.authenticate;

import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import jakarta.validation.constraints.*;
import lombok.Value;

@Value
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blank")
    String username;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password;
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email;
    @NotBlank(message = "Full name cannot be blank")
    String fullName;
    @NotBlank(message = "Identification number cannot be blank")
    String identificationNumber;
    @NotBlank(message = "Phone number cannot be blank")
    String phoneNumber;
    @NotBlank(message = "Organization cannot be blank")
    String organization;
    String photoUrl;


    public UserEntity createEntity(String hashedPassword, RoleEntity role) {
        UserEntity entity = new UserEntity();
        entity.setRole(role);
        entity.setUsername(username);
        entity.setPassword(hashedPassword);
        entity.setEmail(email);
        entity.setFullName(fullName);
        entity.setIdentificationNumber(identificationNumber);
        entity.setPhoneNumber(phoneNumber);
        entity.setOrganization(organization);
        entity.setPhotoUrl(photoUrl);
        entity.setIsActive(true);

        return entity;
    }

}
