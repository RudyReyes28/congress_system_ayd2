package com.alessandro.congress_management.controllers.submissions.evaluation;

import com.alessandro.congress_management.dto.submissions.evaluation.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.submissions.evaluation.EvaluationScientificCommiteeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/committee")
@Tag(name = "Evaluation Scientific Committee", description = "Endpoints for the scientific committee to manage evaluations of submissions.")
public class EvaluationScientificCommiteeController {
    private final EvaluationScientificCommiteeService evaluationService;


    public EvaluationScientificCommiteeController(EvaluationScientificCommiteeService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/me/congresses")
    @Operation(summary = "Get My Committee Congresses", description = "Retrieve a list of congresses for which the authenticated user is a member of the scientific committee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of congresses retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "No congresses found for the authenticated user.")
    })
    public ResponseEntity<List<CongressSummaryResponse>> getMyCommitteeCongresses() throws NotFoundException {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CongressSummaryResponse> congresses = evaluationService.getMyCommitteeCongresses(userId);
        return ResponseEntity.ok(congresses);
    }

    @GetMapping("/congresses/{idCongress}/calls")
    @Operation(summary = "Get Calls by Congress for Committee", description = "Retrieve a list of calls for papers associated with a specific congress for which the authenticated user is a member of the scientific committee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of calls retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "No calls found for the specified congress or the authenticated user is not a member of the scientific committee for that congress.")
    })
    public ResponseEntity<List<CallSummaryResponse>> getCallsByCongressForCommittee(@PathVariable Long idCongress) throws NotFoundException {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CallSummaryResponse> calls = evaluationService.getCallsByCongressForCommittee(idCongress, userId);
        return ResponseEntity.ok(calls);
    }

    @GetMapping("/calls/{idCall}/submissions")
    @Operation(summary = "Get Submissions by Call for Committee", description = "Retrieve a list of submissions associated with a specific call for papers for which the authenticated user is a member of the scientific committee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of submissions retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "No submissions found for the specified call or the authenticated user is not a member of the scientific committee for that call."),
            @ApiResponse(responseCode = "400", description = "The specified call is not open for evaluation or the authenticated user is not assigned as an evaluator for that call.")
    })
    public ResponseEntity<List<SubmissionSummaryResponse>> getSubmissionsByCallForCommittee(@PathVariable Long idCall) throws NotFoundException, BusinessRuleException {
        Long userId = SecurityUtils.getCurrentUserId();
        List<SubmissionSummaryResponse> submissions = evaluationService.getSubmissionsByCallForCommittee(idCall, userId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/{idSubmission}")
    @Operation(summary = "Get Submission Details for Committee", description = "Retrieve detailed information about a specific submission for which the authenticated user is a member of the scientific committee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission details retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Submission not found or the authenticated user is not a member of the scientific committee for the congress associated with the submission."),
            @ApiResponse(responseCode = "400", description = "The authenticated user is not assigned as an evaluator for the call associated with the submission.")
    })
    public ResponseEntity<SubmissionDetailsResponse> getSubmissionDetailsForCommittee(@PathVariable Long idSubmission) throws NotFoundException {
        Long userId = SecurityUtils.getCurrentUserId();
        SubmissionDetailsResponse submissionDetails = evaluationService.getSubmissionDetailsForCommittee(idSubmission, userId);
        return ResponseEntity.ok(submissionDetails);
    }

    @GetMapping("/evaluations/me")
    @Operation(summary = "Get My Evaluations", description = "Retrieve a list of evaluations performed by the authenticated user as a member of the scientific committee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of evaluations retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "No evaluations found for the authenticated user.")
    })
    public ResponseEntity<List<EvaluationDetailsResponse>> getMyEvaluations() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<EvaluationDetailsResponse> evaluations = evaluationService.getMyEvaluations(userId);
        return ResponseEntity.ok(evaluations);
    }

    @PostMapping("/submissions/{idSubmission}/evaluations")
    @Operation(summary = "Evaluate Submission", description = "Evaluate a specific submission as a member of the scientific committee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission evaluated successfully."),
            @ApiResponse(responseCode = "404", description = "Submission not found or the authenticated user is not a member of the scientific committee for the congress associated with the submission."),
            @ApiResponse(responseCode = "400", description = "The specified call is not open for evaluation, the authenticated user is not assigned as an evaluator for the call associated with the submission, or the evaluation request is invalid.")
    })
    public ResponseEntity<EvaluationResponse> evaluateSubmission(@PathVariable Long idSubmission, @RequestBody EvaluationRequest request) throws NotFoundException, BusinessRuleException {
        Long evaluatorId = SecurityUtils.getCurrentUserId();
        EvaluationResponse evaluationResult = evaluationService.evaluateSubmission(idSubmission, evaluatorId, request);
        return ResponseEntity.ok(evaluationResult);
    }




}
