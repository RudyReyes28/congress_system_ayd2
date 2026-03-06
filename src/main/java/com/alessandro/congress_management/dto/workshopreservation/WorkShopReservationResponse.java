package com.alessandro.congress_management.dto.workshopreservation;

import com.alessandro.congress_management.models.workshop_reservation.WorkshopReservationEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class WorkShopReservationResponse {
    Long idReservation;
    Long idActivity;
    String nameActivity;
    String descriptionActivity;
    String userNameReserving;
    String userEmailReserving;
    LocalDateTime reservedAt;

    public static WorkShopReservationResponse fromEntity(WorkshopReservationEntity workshopReservationEntity) {
        return new WorkShopReservationResponse(
                workshopReservationEntity.getIdReservation(),
                workshopReservationEntity.getActivity().getIdActivity(),
                workshopReservationEntity.getActivity().getActivityName(),
                workshopReservationEntity.getActivity().getDescription(),
                workshopReservationEntity.getUser().getFullName(),
                workshopReservationEntity.getUser().getEmail(),
                workshopReservationEntity.getReservedAt()
        );
    }
}
