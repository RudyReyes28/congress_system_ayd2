package com.alessandro.congress_management.dto.reports;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
public class EarningsReportResponse {
    List<InstitutionEarningsDto> institutions;
    BigDecimal totalRevenue;
    BigDecimal totalSystemEarnings;

    @Value
    public static class InstitutionEarningsDto {
        Long idInstitution;
        String institutionName;
        List<CongressEarningsDto> congresses;
        BigDecimal institutionTotalRevenue;
        BigDecimal institutionTotalEarnings;
    }

    @Value
    public static class CongressEarningsDto {
        Long idCongress;
        String congressName;
        String location;
        LocalDate startDate;
        LocalDate endDate;
        BigDecimal price;
        int totalRegistrations;
        BigDecimal totalRevenue;
        BigDecimal systemEarnings;
    }
}