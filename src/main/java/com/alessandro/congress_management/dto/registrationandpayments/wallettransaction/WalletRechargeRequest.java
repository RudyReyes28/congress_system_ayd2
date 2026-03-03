package com.alessandro.congress_management.dto.registrationandpayments.wallettransaction;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class WalletRechargeRequest {
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    BigDecimal amount;
    @NotBlank(message = "Description is required")
    String description;
    @NotNull(message = "Transaction date is required")
    @FutureOrPresent(message = "Transaction date cannot be in the past")
    LocalDateTime transactionDate;

}
