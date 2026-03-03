package com.alessandro.congress_management.dto.registrationandpayments.registration;

import com.alessandro.congress_management.models.registrations_and_payments.RegistrationEntity;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class RegisteredUserDTO {

    Long userId;
    String fullName;
    String email;
    String organization;
    BigDecimal amountPaid;
    LocalDateTime registrationDate;

    public static RegisteredUserDTO fromEntity(RegistrationEntity entity) {
        return new RegisteredUserDTO(
                entity.getUser().getIdUser(),
                entity.getUser().getFullName(),
                entity.getUser().getEmail(),
                entity.getUser().getOrganization(),
                entity.getAmountPaid(),
                entity.getRegistrationDate()
        );
    }
}