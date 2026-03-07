package com.alessandro.congress_management.dto.reports;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class CongressEarningsReportResponse {
    Long idCongress;
    String congressName;
    String location;
    LocalDate startDate;
    LocalDate  endDate;
    BigDecimal price;
    int totalRegistrations;
    BigDecimal totalRevenue;
    BigDecimal commissionAmount;
    BigDecimal netEarnings;
}