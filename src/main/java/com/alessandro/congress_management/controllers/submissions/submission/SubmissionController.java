package com.alessandro.congress_management.controllers.submissions.submission;

import com.alessandro.congress_management.dto.submissions.submission.SubmissionDetails;
import com.alessandro.congress_management.dto.submissions.submission.SubmissionRequest;
import com.alessandro.congress_management.dto.submissions.submission.SubmissionResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.FileStorageException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.submissions.submission.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@Tag(name = "Submission Management", description = "Endpoints for managing submissions")
public class SubmissionController {
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping(value = "/calls/{idCall}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Submit a work to a call for papers",
            description = "Submits a new paper or workshop proposal. The call must be open. File upload is optional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Submission created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or closed call"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Call or user not found")
    })
    public ResponseEntity<SubmissionResponse> submit(
            @PathVariable Long idCall,
            @Valid @RequestPart("request") SubmissionRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws NotFoundException, BusinessRuleException, FileStorageException {
        Long idUser = SecurityUtils.getCurrentUserId();

        SubmissionResponse response = submissionService.submit(idCall, idUser, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{idSubmission}/resubmit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Resubmit a rejected work",
            description = "Replaces a rejected submission with a new one. The call must still be open."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resubmission successful"),
            @ApiResponse(responseCode = "400", description = "Submission is not rejected or call is closed"),
            @ApiResponse(responseCode = "403", description = "Access denied or not the author"),
            @ApiResponse(responseCode = "404", description = "Submission or user not found")
    })
    public ResponseEntity<SubmissionResponse> resubmit(
            @PathVariable Long idSubmission,
            @Valid @RequestPart("request") SubmissionRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws NotFoundException, BusinessRuleException, FileStorageException {
        Long idUser = SecurityUtils.getCurrentUserId();

        SubmissionResponse response = submissionService.resubmit(idSubmission, idUser, request, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calls/{idCall}")
    @Operation(
            summary = "Get all submissions for a call",
            description = "Returns all submissions belonging to a specific call for papers. Admin only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Call not found")
    })
    public ResponseEntity<List<SubmissionResponse>> getByCall(@PathVariable Long idCall) throws NotFoundException {
        return ResponseEntity.ok(submissionService.getSubmissionsByCall(idCall));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get all submissions by a user",
            description = "Returns every submission made by the given user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<SubmissionDetails>> getByUser() throws NotFoundException {
        Long idUser = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(submissionService.getSubmissionsByUser(idUser));
    }
}
