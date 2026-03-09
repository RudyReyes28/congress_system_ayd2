package com.alessandro.congress_management.dto.reports;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
public class CongressByInstitutionReportResponse {
    List<InstitutionCongressesDto> institutions;
    int totalCongresses;

    @Value
    public static class InstitutionCongressesDto {
        Long idInstitution;
        String institutionName;
        List<CongressSummaryDto> congresses;
    }

    @Value
    public static class CongressSummaryDto {
        Long idCongress;
        String  congressName;
        String description;
        LocalDate startDate;
        LocalDate endDate;
        String location;
        BigDecimal price;
        Boolean isActive;
    }
}
