package com.alessandro.congress_management.dto.registrationandpayments.registration;

import com.alessandro.congress_management.models.registrations_and_payments.RegistrationEntity;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class RegistrationResponse {
    Long idRegistration;
    Long congressId;
    Long userId;
    String congressName;
    String userName;
    String userEmail;
    BigDecimal amountPaid;
    LocalDateTime registrationDate;

    public static RegistrationResponse fromEntity(RegistrationEntity entity) {
        return new RegistrationResponse(
                entity.getIdRegistration(),
                entity.getCongress().getIdCongress(),
                entity.getUser().getIdUser(),
                entity.getCongress().getCongressName(),
                entity.getUser().getFullName(),
                entity.getUser().getEmail(),
                entity.getAmountPaid(),
                entity.getRegistrationDate()
        );
    }
}
