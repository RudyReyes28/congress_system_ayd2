package com.alessandro.congress_management.dto.reports;

import lombok.Value;

import java.util.List;

@Value
public class ParticipantsReportResponse {
    Long idCongress;
    String congressName;
    int totalParticipants;
    List<ParticipantDto> participants;

    @Value
    public static class ParticipantDto {
        Long idUser;
        String identificationNumber;
        String fullName;
        String organization;
        String email;
        String phoneNumber;
        List<String> participationTypes; // ["ATTENDEE"], ["PRESENTER","ATTENDEE"], etc.
    }
}