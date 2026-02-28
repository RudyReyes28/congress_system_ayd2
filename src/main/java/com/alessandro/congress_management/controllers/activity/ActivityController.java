package com.alessandro.congress_management.controllers.activity;

import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.activity.CreateActivityRequest;
import com.alessandro.congress_management.dto.activity.UpdateActivityRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.activity.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/congresses")
@Tag(name = "ActivityController", description = "Controller for managing activities within congresses")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/{congressId}/activities")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Create a new activity within a congress", description = "Creates a new activity within a specified congress. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Activity created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<ActivityResponse> createActivity(@PathVariable Long congressId, @Valid @RequestBody CreateActivityRequest request) throws NotFoundException, BusinessRuleException, BusinessRuleException, NotFoundException {
        ActivityResponse activityResponse = activityService.createActivity(congressId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(activityResponse);
    }

    @PutMapping("/activities/{activityId}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Update an existing activity", description = "Updates the details of an existing activity. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<ActivityResponse> updateActivity(@PathVariable Long activityId, @Valid @RequestBody UpdateActivityRequest request) throws NotFoundException, BusinessRuleException {
        ActivityResponse activityResponse = activityService.updateActivity(activityId, request);
        return ResponseEntity.ok(activityResponse);
    }

    @DeleteMapping("/activities/{activityId}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Delete an existing activity", description = "Deletes an existing activity from the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Activity deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) throws NotFoundException, BusinessRuleException {
        activityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }
    //Sin distincion de rol
    @GetMapping("rooms/{roomId}/activities")
    @Operation(summary = "Get activities by room ID", description = "Retrieves a list of activities scheduled in a specific room.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of activities retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<List<ActivityResponse>> getActivitiesByRoomId(@PathVariable Long roomId) throws NotFoundException {
        List<ActivityResponse> activities = activityService.getActivitiesByRoomId(roomId);
        return ResponseEntity.ok(activities);
    }

    //Sin distincion de rol
    @GetMapping("/{congressId}/activities")
    @Operation(summary = "Get activities by congress ID", description = "Retrieves a list of activities scheduled for a specific congress.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of activities retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<List<ActivityResponse>> getActivitiesByCongressId(@PathVariable Long congressId) throws NotFoundException {
        List<ActivityResponse> activities = activityService.getActivitiesByCongressId(congressId);
        return ResponseEntity.ok(activities);
    }

}
