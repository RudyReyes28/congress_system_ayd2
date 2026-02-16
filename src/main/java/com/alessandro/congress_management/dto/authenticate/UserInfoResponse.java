package com.alessandro.congress_management.dto.authenticate;

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

    public static UserInfoResponse fromEntity(UserEntity user) {
        return new UserInfoResponse(
                user.getIdUser(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getIdentificationNumber(),
                user.getPhoneNumber(),
                user.getOrganization(),
                user.getPhotoUrl(),
                user.getWalletBalance()
        );
    }

}
