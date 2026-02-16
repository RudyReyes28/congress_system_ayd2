package com.alessandro.congress_management.dto.authenticate;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import lombok.Value;

@Value
public class RegisterRequest {
    String username;
    String password;
    String email;
    String fullName;
    String identificationNumber;
    String phoneNumber;
    String organization;
    String photoUrl;


    public UserEntity createEntity(String hashedPassword) {
        UserEntity entity = new UserEntity();
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
