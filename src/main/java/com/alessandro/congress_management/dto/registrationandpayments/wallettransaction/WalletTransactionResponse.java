package com.alessandro.congress_management.dto.registrationandpayments.wallettransaction;

import com.alessandro.congress_management.models.registrations_and_payments.WalletTransactionEntity;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class WalletTransactionResponse {
    Long idTransaction;
    Long userId;
    String userFullName;
    String transactionType;
    String description;
    String relatedRegistrationInfo;
    LocalDateTime transactionDate;
    BigDecimal amount;

    public static WalletTransactionResponse fromEntity(WalletTransactionEntity entity) {
        return new WalletTransactionResponse(
                entity.getIdTransaction(),
                entity.getUser().getIdUser(),
                entity.getUser().getFullName(),
                entity.getTransactionType(),
                entity.getDescription(),
                entity.getRelatedRegistration() != null ? entity.getRelatedRegistration().getCongress().getCongressName() : null,
                entity.getTransactionDate(),
                entity.getAmount()
        );
    }
}
