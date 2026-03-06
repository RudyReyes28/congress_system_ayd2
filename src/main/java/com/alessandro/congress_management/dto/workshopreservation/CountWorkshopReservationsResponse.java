package com.alessandro.congress_management.dto.workshopreservation;

import lombok.Value;

@Value
public class CountWorkshopReservationsResponse {
    Long idActivity;
    String nameActivity;
    int countReservations;

    public static CountWorkshopReservationsResponse fromEntity(Long idActivity, String nameActivity, int countReservations) {
        return new CountWorkshopReservationsResponse(
                idActivity,
                nameActivity,
                countReservations
        );
    }
}
