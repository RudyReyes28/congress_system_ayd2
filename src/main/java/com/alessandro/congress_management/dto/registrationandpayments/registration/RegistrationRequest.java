package com.alessandro.congress_management.dto.registrationandpayments.registration;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class RegistrationRequest {
    @NotNull(message = "Registration date cannot be null")
    @FutureOrPresent
    LocalDateTime registrationDate;
}
