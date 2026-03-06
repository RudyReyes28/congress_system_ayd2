package com.alessandro.congress_management.controllers.workshopreservation;

import com.alessandro.congress_management.dto.workshopreservation.CountWorkshopReservationsResponse;
import com.alessandro.congress_management.dto.workshopreservation.WorkShopReservationDetailsResponse;
import com.alessandro.congress_management.dto.workshopreservation.WorkShopReservationResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.workshopreservation.WorkShopReservationService;
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
@Tag(name = "Workshop Reservation", description = "Endpoints for managing workshop reservations")
public class WorkshopReservationController {
    private final WorkShopReservationService workshopReservationService;

    public WorkshopReservationController(WorkShopReservationService workshopReservationService) {
        this.workshopReservationService = workshopReservationService;
    }

    @PostMapping("/workshops/{idActivity}/reservations")
    @Operation(summary = "Reserve a workshop", description = "Allows a user to reserve a spot in a workshop activity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Workshop reserved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or workshop is full"),
            @ApiResponse(responseCode = "404", description = "User or activity not found")
    })
    public ResponseEntity<WorkShopReservationResponse> reserveWorkshop(@PathVariable Long idActivity) throws BusinessRuleException, NotFoundException {
        Long idUser = SecurityUtils.getCurrentUserId();
        WorkShopReservationResponse response = workshopReservationService.reserveWorkshop(idActivity, idUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/workshops/reservations/{idReservation}")
    @Operation(summary = "Cancel a workshop reservation", description = "Allows a user to cancel their workshop reservation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Workshop reservation cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or cancellation not allowed"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    public ResponseEntity<Void> cancelWorkshopReservation(@PathVariable Long idReservation) throws BusinessRuleException, NotFoundException {
        Long idUser = SecurityUtils.getCurrentUserId();
        workshopReservationService.cancelWorkshopReservation(idReservation, idUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/workshops/reservations")
    @Operation(summary = "Get my workshop reservations", description = "Retrieves a list of the user's workshop reservations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of workshop reservations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<WorkShopReservationDetailsResponse>> getMyWorkshopReservations() {
        Long idUser = SecurityUtils.getCurrentUserId();
        List<WorkShopReservationDetailsResponse> reservations = workshopReservationService.getMyWorkshopReservations(idUser);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/workshops/{idActivity}/reservations/count")
    @Operation(summary = "Count workshop reservations", description = "Counts the number of reservations for a specific workshop activity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Number of workshop reservations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<CountWorkshopReservationsResponse> countWorkshopReservations(@PathVariable Long idActivity) throws NotFoundException {
        CountWorkshopReservationsResponse response = workshopReservationService.countWorkshopReservations(idActivity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workshops/{idActivity}/reservations")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get all workshop reservations by activity", description = "Retrieves a list of all reservations for a specific workshop activity. Admins can see all reservations, while regular users can only see their own reservations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of workshop reservations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<WorkShopReservationResponse>> getAllWorkshopReservationsByActivity(@PathVariable Long idActivity) throws NotFoundException, BusinessRuleException {
        Long idUser = SecurityUtils.getCurrentUserId();
        List<WorkShopReservationResponse> reservations = workshopReservationService.getAllWorkshopReservationsByActivity(idActivity, idUser);
        return ResponseEntity.ok(reservations);
    }

}
