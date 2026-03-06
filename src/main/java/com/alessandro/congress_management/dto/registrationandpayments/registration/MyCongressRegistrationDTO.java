package com.alessandro.congress_management.dto.registrationandpayments.registration;

import com.alessandro.congress_management.models.registrations_and_payments.RegistrationEntity;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class MyCongressRegistrationDTO {

    Long registrationId;
    Long congressId;
    String congressName;
    LocalDate startDate;
    LocalDate endDate;
    String location;
    BigDecimal amountPaid;
    LocalDateTime registrationDate;

    public static MyCongressRegistrationDTO fromEntity(RegistrationEntity entity) {
        return new MyCongressRegistrationDTO(
                entity.getIdRegistration(),
                entity.getCongress().getIdCongress(),
                entity.getCongress().getCongressName(),
                entity.getCongress().getStartDate(),
                entity.getCongress().getEndDate(),
                entity.getCongress().getLocation(),
                entity.getAmountPaid(),
                entity.getRegistrationDate()
        );
    }
}