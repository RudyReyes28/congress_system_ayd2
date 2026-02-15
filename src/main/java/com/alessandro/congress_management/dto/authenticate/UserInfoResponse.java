package com.alessandro.congress_management.dto.authenticate;

import com.alessandro.congress_management.models.authentication_and_users.PersonEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class UserInfoResponse {
    Long id;
    String username;
    String email;
    String fullName;
    String identificationNumber;
    String phoneNumber;
    String organization;
    String photoUrl;
    BigDecimal walletBalance;

    public static UserInfoResponse fromEntities(UserEntity user, PersonEntity person) {
        return new UserInfoResponse(
                user.getIdUser(),
                user.getUsername(),
                person.getEmail(),
                person.getFullName(),
                user.getIdentificationNumber(),
                person.getPhoneNumber(),
                person.getOrganization(),
                person.getPhotoUrl(),
                user.getWalletBalance()
        );
    }

}
