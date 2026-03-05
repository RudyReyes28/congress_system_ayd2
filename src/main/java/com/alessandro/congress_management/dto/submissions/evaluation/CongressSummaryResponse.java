package com.alessandro.congress_management.dto.submissions.evaluation;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class CongressSummaryResponse {
    Long idCongress;
    String congressName;
    LocalDate startDate;
    LocalDate endDate;

    public static CongressSummaryResponse fromEntity(Long idCongress, String congressName, LocalDate startDate, LocalDate endDate) {
        return new CongressSummaryResponse(
                idCongress,
                congressName,
                startDate,
                endDate);
    }
}
