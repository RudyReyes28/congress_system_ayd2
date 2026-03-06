package com.alessandro.congress_management.controllers.activity;

import com.alessandro.congress_management.dto.activity.ActivityTypeResponse;
import com.alessandro.congress_management.services.activity.ActivityTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activity-types")
@Tag(name = "ActivityTypeController", description = "Controller for managing activity types within congresses")
public class ActivityTypeController {
    private final ActivityTypeService activityTypeService;

    public ActivityTypeController(ActivityTypeService activityTypeService) {
        this.activityTypeService = activityTypeService;
    }

    @GetMapping
    @Operation(summary = "Get all activity types", description = "Retrieves a list of all activity types in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity types retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<ActivityTypeResponse>> getAllActivityTypes() {
        List<ActivityTypeResponse> activityTypes = activityTypeService.getAllActivityTypes();
        return ResponseEntity.ok(activityTypes);
    }
}
