package com.alessandro.congress_management.dto.authenticate;

import com.alessandro.congress_management.models.authentication_and_users.PersonEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import lombok.Value;

@Value
public class RegisterRequest {
    String email;
    String fullName;
    String phoneNumber;
    String organization;
    String photoUrl=null;
    String username;
    String password;
    String identificationNumber;

    public PersonEntity createPersonEntity() {
        PersonEntity person = new PersonEntity();
        person.setEmail(this.email);
        person.setFullName(this.fullName);
        person.setPhoneNumber(this.phoneNumber);
        person.setOrganization(this.organization);
        person.setPhotoUrl(null);
        return person;
    }

    public UserEntity createUserEntity() {
        UserEntity user = new UserEntity();
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setIdentificationNumber(this.identificationNumber);
        user.setIsActive(true);
        user.setWalletBalance(null);
        return user;
    }

}
