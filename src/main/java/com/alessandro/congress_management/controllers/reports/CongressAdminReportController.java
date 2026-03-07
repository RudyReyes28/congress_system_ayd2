package com.alessandro.congress_management.controllers.reports;

import com.alessandro.congress_management.dto.reports.ActivityAttendanceReportResponse;
import com.alessandro.congress_management.dto.reports.CongressEarningsReportResponse;
import com.alessandro.congress_management.dto.reports.ParticipantsReportResponse;
import com.alessandro.congress_management.dto.reports.WorkshopReservationReportResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.reports.CongressAdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/congress-admin/congresses/{idCongress}/reports")
@Tag(name = "Congress Admin Reports", description = "Endpoints for generating reports related to congresses for Congress Admins")
public class CongressAdminReportController {
    private final CongressAdminReportService reportService;

    public CongressAdminReportController(CongressAdminReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/participants")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get participants report", description = "Generates a report of participants for a specific congress, optionally filtered by participation type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participants report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Congress or Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ParticipantsReportResponse> getParticipantsReport(
            @PathVariable Long idCongress,
            @RequestParam(required = false) String participationType
    ) throws NotFoundException, BusinessRuleException {
        Long idAdmin = SecurityUtils.getCurrentUserId();

        ParticipantsReportResponse response =
                reportService.getParticipantsReport(idCongress, idAdmin, participationType);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/attendance")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get activity attendance report", description = "Generates a report of attendance for activities within a specific congress, optionally filtered by activity, room, and date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity attendance report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Congress or Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ActivityAttendanceReportResponse> getActivityAttendanceReport(
            @PathVariable Long idCongress,
            @RequestParam(required = false) Long idActivity,
            @RequestParam(required = false) Long idRoom,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate
    ) throws NotFoundException, BusinessRuleException {
        Long idAdmin = SecurityUtils.getCurrentUserId();

        ActivityAttendanceReportResponse response =
                reportService.getActivityAttendanceReport(
                        idCongress, idAdmin, idActivity, idRoom, startDate, endDate
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/workshops/reservations")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get activity attendance report", description = "Generates a report of attendance for activities within a specific congress, optionally filtered by activity, room, and date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity attendance report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Congress or Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<WorkshopReservationReportResponse> getWorkshopReservationReport(
            @PathVariable Long idCongress,
            @RequestParam(required = false) Long idActivity
    ) throws NotFoundException, BusinessRuleException {
        Long idAdmin = SecurityUtils.getCurrentUserId();

        WorkshopReservationReportResponse response =
                reportService.getWorkshopReservationReport(idCongress, idAdmin, idActivity);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/earnings")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get activity attendance report", description = "Generates a report of attendance for activities within a specific congress, optionally filtered by activity, room, and date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity attendance report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Congress or Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<CongressEarningsReportResponse> getCongressEarningsReport(
            @PathVariable Long idCongress
    ) throws NotFoundException, BusinessRuleException {
        Long idAdmin = SecurityUtils.getCurrentUserId();

        CongressEarningsReportResponse response =
                reportService.getCongressEarningsReport(idCongress, idAdmin);

        return ResponseEntity.ok(response);
    }
}
