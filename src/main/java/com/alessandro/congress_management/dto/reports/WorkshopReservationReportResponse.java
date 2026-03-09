package com.alessandro.congress_management.dto.reports;

import lombok.Value;

import java.util.List;

@Value
public class WorkshopReservationReportResponse {
    Long idCongress;
    String congressName;
    List<WorkshopSummaryDto> workshops;

    @Value
    public static class WorkshopSummaryDto {
        Long idActivity;
        String workshopName;
        int totalCapacity;
        int reservationCount;
        int availableSpots;
        List<ReservedParticipantDto> reservedParticipants;
    }

    @Value
    public static class ReservedParticipantDto {
        Long idUser;
        String identificationNumber;
        String fullName;
        String email;
    }
}