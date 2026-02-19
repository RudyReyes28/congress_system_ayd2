package com.alessandro.congress_management.controllers.institution;

import com.alessandro.congress_management.dto.institution.InstitutionResponse;
import com.alessandro.congress_management.services.institution.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/institutions")
@Tag(name = "Institution Controller", description = "Controller for managing institutions")
public class InstitutionController {
    private final InstitutionService institutionService;

    @Autowired
    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Get all institutions", description = "Retrieves a list of all institutions in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of institutions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<InstitutionResponse>> getActiveInstitutions() {
        List<InstitutionResponse> institutions = institutionService.getActiveInstitutions()
                .stream()
                .map(InstitutionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(institutions);
    }
}
