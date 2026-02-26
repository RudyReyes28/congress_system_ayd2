package com.alessandro.congress_management.controllers.scientificcommitee;

import com.alessandro.congress_management.dto.scientificcommitee.EligibleUserCommitteeResponse;
import com.alessandro.congress_management.dto.scientificcommitee.ScientificCommiteeResponse;
import com.alessandro.congress_management.dto.user_manager.UserResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.scientificcommitee.ScientificCommiteeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/congresses")
@Tag(name = "Scientific Committee Management", description = "Endpoints for managing scientific committee members of congresses")
public class ScientificCommiteeController {
    private final ScientificCommiteeService scientificCommiteeService;


    public ScientificCommiteeController(ScientificCommiteeService scientificCommiteeService) {
        this.scientificCommiteeService = scientificCommiteeService;
    }

    @PostMapping("/{congressId}/committee/{userId}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Assign a user as a scientific committee member to a congress", description = "Assigns a user as a scientific committee member to a congress. Only users with the ADMIN_CONGRESS role can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User assigned as a scientific committee member successfully"),
            @ApiResponse(responseCode = "404", description = "Congress or user not found"),
            @ApiResponse(responseCode = "400", description = "Business rule violation")
    })
    public ResponseEntity<ScientificCommiteeResponse> createScientificCommiteeMember(@PathVariable Long congressId, @PathVariable Long userId) throws NotFoundException, BusinessRuleException {
        ScientificCommiteeResponse response = scientificCommiteeService.createScientificCommiteeMember(congressId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{congressId}/committee")
    @PreAuthorize("hasAnyRole('ADMIN_CONGRESS', 'ADMIN_SYSTEM')")
    @Operation(summary = "Get all scientific committee members of a congress", description = "Retrieves a list of all scientific committee members assigned to a specific congress. Only users with the ADMIN_CONGRESS role can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of scientific committee members retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<List<ScientificCommiteeResponse>> getScientificCommiteeMembersByCongressId(@PathVariable Long congressId) throws NotFoundException {
        List<ScientificCommiteeResponse> response = scientificCommiteeService.getScientificCommiteeMembersByCongressId(congressId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{congressId}/committee/{userId}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Remove a scientific committee member from a congress", description = "Removes a scientific committee member from a congress. Only users with the ADMIN_CONGRESS role can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Scientific committee member removed successfully"),
            @ApiResponse(responseCode = "404", description = "Congress or user not found")
    })
    public ResponseEntity<Void> removeScientificCommiteeMember(@PathVariable Long congressId, @PathVariable Long userId) throws NotFoundException {
        scientificCommiteeService.deleteScientificCommiteeMember(congressId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{congressId}/committee/eligible-users")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get eligible users for scientific committee membership", description = "Retrieves a list of users who are eligible to be assigned as scientific committee members for a specific congress. Only users with the ADMIN_CONGRESS role can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of eligible users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<List<EligibleUserCommitteeResponse>> getEligibleUsersForCommittee(@PathVariable Long congressId) throws NotFoundException {
        List<EligibleUserCommitteeResponse> response = scientificCommiteeService.getEligibleUsersForCommittee(congressId);
        return ResponseEntity.ok(response);
    }

}
