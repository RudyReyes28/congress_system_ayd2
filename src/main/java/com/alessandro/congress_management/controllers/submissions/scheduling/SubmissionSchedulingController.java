package com.alessandro.congress_management.controllers.submissions.scheduling;

import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.submissions.scheduling.EvaluationSubmissionDetails;
import com.alessandro.congress_management.dto.submissions.scheduling.ScheduleSubmissionsRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.submissions.scheduling.SubmissionSchedulingService;
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
@RequestMapping("/api/v1/admin")
@Tag(name = "Submission Scheduling", description = "Endpoints for scheduling submissions into activities")
public class SubmissionSchedulingController {
    private final SubmissionSchedulingService schedulingService;

    public SubmissionSchedulingController(SubmissionSchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping("/calls/{idCall}/evaluation-submissions")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get evaluation details for submissions of a specific call for papers", description = "Returns a list of evaluation details for all submissions associated with the specified call for papers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved evaluation details for submissions"),
            @ApiResponse(responseCode = "404", description = "Call for papers not found")
    })
    public ResponseEntity<List<EvaluationSubmissionDetails>> getEvaluationSubmissionsByIdCall(@PathVariable Long idCall) throws NotFoundException {
        List<EvaluationSubmissionDetails> details = schedulingService.getEvaluationSubmissionsByIdCall(idCall);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/submissions/{idSubmission}/schedule")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Schedule an approved submission into an activity", description = "Schedules an approved submission into an activity based on the provided scheduling details. The submission must be in an approved state to be scheduled.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully scheduled the submission into an activity"),
            @ApiResponse(responseCode = "400", description = "Invalid scheduling details or submission is not approved"),
            @ApiResponse(responseCode = "404", description = "Submission not found")
    })
    public ResponseEntity<ActivityResponse> scheduleSubmission(@PathVariable Long idSubmission, @Valid @RequestBody ScheduleSubmissionsRequest request) throws NotFoundException, BusinessRuleException {
        ActivityResponse response = schedulingService.scheduleSubmissions(idSubmission, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submissions/{idSubmission}/cancel")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Cancel the scheduling of a submission", description = "Cancels the scheduling of a submission, removing it from the associated activity. The submission must be currently scheduled to be cancelled.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully cancelled the scheduling of the submission"),
            @ApiResponse(responseCode = "400", description = "Submission is not currently scheduled"),
            @ApiResponse(responseCode = "404", description = "Submission not found")
    })
    public ResponseEntity<Void> cancelScheduling(@PathVariable Long idSubmission) throws NotFoundException {
        schedulingService.cancelScheduling(idSubmission);
        return ResponseEntity.ok().build();
    }

}
