package com.alessandro.congress_management.dto.registrationandpayments.wallettransaction;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class WalletBalanceResponse {
    Long userId;
    String fullName;
    String email;
    BigDecimal balance;

    public static WalletBalanceResponse fromEntity(UserEntity user){
        return new WalletBalanceResponse(
                user.getIdUser(),
                user.getFullName(),
                user.getEmail(),
                user.getWalletBalance()
        );
    }
}
