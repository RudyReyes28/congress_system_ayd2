package com.alessandro.congress_management.controllers.reports;


import com.alessandro.congress_management.dto.reports.CongressByInstitutionReportResponse;
import com.alessandro.congress_management.dto.reports.EarningsReportResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.reports.SystemAdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/system-admin/reports")
public class SystemAdminReportController {

    private final SystemAdminReportService reportService;


    public SystemAdminReportController(SystemAdminReportService systemAdminReportService) {
        this.reportService = systemAdminReportService;
    }

    @GetMapping("/earnings")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Get earnings report", description = "Generates a report of earnings for congresses within a specified date range, optionally filtered by institution.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Earnings report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Institution or System Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<EarningsReportResponse> getEarningsReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long idInstitution
    ) throws NotFoundException, BusinessRuleException {
        Long idAdmin = SecurityUtils.getCurrentUserId();
        EarningsReportResponse response = reportService.getEarningsReport(startDate, endDate, idInstitution, idAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/congresses-by-institution")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Get congresses by institution report", description = "Generates a report of congresses grouped by institution within a specified date range, optionally filtered by institution.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Congress by institution report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Institution or System Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<CongressByInstitutionReportResponse> getCongressByInstitutionReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long idInstitution
    ) throws NotFoundException, BusinessRuleException {
        Long idAdmin = SecurityUtils.getCurrentUserId();

        CongressByInstitutionReportResponse response =
                reportService.getCongressByInstitutionReport(startDate, endDate, idInstitution, idAdmin);

        return ResponseEntity.ok(response);
    }
}
