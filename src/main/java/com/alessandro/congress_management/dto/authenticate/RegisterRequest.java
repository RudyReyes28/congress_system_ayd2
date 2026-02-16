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

    /*

    Ejemplo de json para el request:
{
    "username": "john_doe",
    "password": "securePassword123",
    "email": "
    "fullName": "John Doe",
    "identificationNumber": "123456789",
    "phoneNumber": "+1234567890",
    "organization": "Tech Company",
    "photoUrl": "http://example.com/photo.jpg"
}
     */

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
