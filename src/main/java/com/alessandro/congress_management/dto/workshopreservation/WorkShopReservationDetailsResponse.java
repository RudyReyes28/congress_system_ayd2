package com.alessandro.congress_management.dto.workshopreservation;

import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.models.workshop_reservation.WorkshopReservationEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class WorkShopReservationDetailsResponse {
    Long idReservation;
    LocalDateTime reservedAt;
    ActivityResponse activity;

    public static WorkShopReservationDetailsResponse fromEntity(WorkshopReservationEntity workshopReservationEntity) {
        return new WorkShopReservationDetailsResponse(
                workshopReservationEntity.getIdReservation(),
                workshopReservationEntity.getReservedAt(),
                ActivityResponse.fromEntity(workshopReservationEntity.getActivity())
        );
    }

}
