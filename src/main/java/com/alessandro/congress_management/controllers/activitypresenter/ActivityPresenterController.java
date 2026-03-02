package com.alessandro.congress_management.controllers.activitypresenter;

import com.alessandro.congress_management.dto.activitypresenter.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.activitypresenter.ActivityPresenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@Tag(name = "ActivityPresenterController", description = "Controller for managing presenters assigned to activities within congresses")
public class ActivityPresenterController {
    private final ActivityPresenterService activityPresenterService;

    public ActivityPresenterController(ActivityPresenterService activityPresenterService) {
        this.activityPresenterService = activityPresenterService;
    }

    @PostMapping("/{idActivity}/presenters")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Assign an existing user as presenter to an activity", description = "Assigns an existing user as a presenter to a specified activity. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presenter assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Activity or user not found")
    })
    public ResponseEntity<ActivityPresenterResponse> assignExistingUserAsPresenter(@PathVariable Long idActivity, @Valid @RequestBody ActivityPresenterRequest request) throws BusinessRuleException, NotFoundException {
        ActivityPresenterResponse response = activityPresenterService.assignExistingUser(request, idActivity);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{idActivity}/presenters/invite")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Assign an invited user as presenter to an activity", description = "Assigns an invited user as a presenter to a specified activity. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presenter assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<ActivityPresenterResponse> assignInvitedUserAsPresenter(@PathVariable Long idActivity, @Valid @RequestBody AssignInviteUserActivityRequest request) throws BusinessRuleException, NotFoundException, DuplicatedEntityException {
        ActivityPresenterResponse response = activityPresenterService.assignInvitedUser(request, idActivity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/presenters/{idActivityPresenter}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Remove a presenter from an activity", description = "Removes a presenter from a specified activity. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Presenter removed successfully"),
            @ApiResponse(responseCode = "400", description = "Business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Activity presenter not found")
    })
    public ResponseEntity<Void> removePresenterFromActivity(@PathVariable Long idActivityPresenter) throws BusinessRuleException, NotFoundException {
        activityPresenterService.removePresenter(idActivityPresenter);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idActivity}/presenters")
    @Operation(summary = "Get presenters assigned to an activity", description = "Retrieves a list of presenters assigned to a specified activity. Accessible by administrators, presenters, and attendees.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presenters retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<List<DetailActivityPresenterResponse>> getPresentersByActivity(@PathVariable Long idActivity) {
        List<DetailActivityPresenterResponse> presenters = activityPresenterService.getPresentersByActivity(idActivity);
        return ResponseEntity.ok(presenters);
    }


    @GetMapping("/{idActivity}/eligible-registered-users")
    @Operation(summary = "Get eligible users for presenter assignment", description = "Retrieves a list of users eligible for assignment as presenters to a specified activity. Accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eligible users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    public ResponseEntity<List<EligibleUsersActivityResponse>> getEligibleUsersForPresenterAssignment(@PathVariable Long idActivity) throws NotFoundException {
        List<EligibleUsersActivityResponse> eligibleUsers = activityPresenterService.getEligibleRegisteredUsers(idActivity);
        return ResponseEntity.ok(eligibleUsers);
    }

    @GetMapping("/{idActivity}/eligible-invited-users")
    @Operation(summary = "Get eligible invited users for presenter assignment", description = "Retrieves a list of invited users eligible for assignment as presenters to a specified activity. Accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eligible invited users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    public ResponseEntity<List<EligibleUsersActivityResponse>> getEligibleInvitedUsersForPresenterAssignment(@PathVariable Long idActivity) throws NotFoundException {
        List<EligibleUsersActivityResponse> eligibleInvitedUsers = activityPresenterService.getEligibleInvitedUsers(idActivity);
        return ResponseEntity.ok(eligibleInvitedUsers);
    }
}
