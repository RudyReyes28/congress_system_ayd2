package com.alessandro.congress_management.services.reports;

import com.alessandro.congress_management.dto.reports.CongressByInstitutionReportResponse;
import com.alessandro.congress_management.dto.reports.EarningsReportResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.time.LocalDate;

public interface SystemAdminReportService {

    EarningsReportResponse getEarningsReport(LocalDate startDate, LocalDate endDate,Long idInstitution, Long idAdmin)
            throws NotFoundException, BusinessRuleException;

    CongressByInstitutionReportResponse getCongressByInstitutionReport(LocalDate startDate, LocalDate endDate, Long idInstitution, Long idAdmin)
            throws NotFoundException, BusinessRuleException;
}