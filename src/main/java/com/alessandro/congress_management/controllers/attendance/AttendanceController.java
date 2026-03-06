package com.alessandro.congress_management.controllers.attendance;

import com.alessandro.congress_management.dto.attendance.AttendanceDetailsResponse;
import com.alessandro.congress_management.dto.attendance.AttendanceRequest;
import com.alessandro.congress_management.dto.attendance.AttendanceResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.attendance.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Attendance", description = "Endpoints for managing attendance records")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/activities/{idActivity}/attendance")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Record attendance for an activity", description = "Allows a user to record their attendance for a specific activity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attendance recorded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or attendance already recorded"),
            @ApiResponse(responseCode = "404", description = "Activity or user not found")
    })
    public ResponseEntity<AttendanceResponse> recordAttendance(@PathVariable Long idActivity, @RequestBody AttendanceRequest attendanceRequest) throws NotFoundException, BusinessRuleException, BusinessRuleException {
        Long idUserAdmin = SecurityUtils.getCurrentUserId();
        AttendanceResponse response = attendanceService.recordAttendance(idActivity, idUserAdmin, attendanceRequest.getIdUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/attendance")
    @Operation(summary = "Get my attendance details", description = "Retrieves a list of the user's attendance details for all activities.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of attendance details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<AttendanceDetailsResponse>> getMyAttendanceDetails() {
        Long idUser = SecurityUtils.getCurrentUserId();
        List<AttendanceDetailsResponse> response = attendanceService.getMyAttendanceDetails(idUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activities/{idActivity}/attendance")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get attendance details by activity", description = "Retrieves a list of attendance details for a specific activity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of attendance details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<List<AttendanceDetailsResponse>> getAttendanceDetailsByActivity(@PathVariable Long idActivity) throws NotFoundException {
        Long idUserAdmin = SecurityUtils.getCurrentUserId();
        List<AttendanceDetailsResponse> response = attendanceService.getAttendanceDetailsByActivity(idActivity, idUserAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/congresses/{idCongress}/attendance")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get attendance details by congress", description = "Retrieves a list of attendance details for all activities within a specific congress.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of attendance details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<List<AttendanceDetailsResponse>> getAttendanceDetailsByCongress(@PathVariable Long idCongress) throws NotFoundException {
        Long idUserAdmin = SecurityUtils.getCurrentUserId();
        List<AttendanceDetailsResponse> response = attendanceService.getAttendanceDetailsByCongress(idCongress, idUserAdmin);
        return ResponseEntity.ok(response);
    }



}
