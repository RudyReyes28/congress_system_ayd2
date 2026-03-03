package com.alessandro.congress_management.dto.registrationandpayments.wallettransaction;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.registrations_and_payments.RegistrationEntity;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class WalletTransactionCreate {
    UserEntity user;
    String transactionType;
    String description;
    RegistrationEntity relatedRegistration;
    LocalDateTime transactionDate;
    BigDecimal amount;

    public WalletTransactionCreate(UserEntity user, String transactionType, String description, RegistrationEntity relatedRegistration, LocalDateTime transactionDate, BigDecimal amount) {
        this.user = user;
        this.transactionType = transactionType;
        this.description = description;
        this.relatedRegistration = relatedRegistration;
        this.transactionDate = transactionDate;
        this.amount = amount;
    }

}
