package com.alessandro.congress_management.controllers.institution;

import com.alessandro.congress_management.dto.institution.CreateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.InstitutionResponse;
import com.alessandro.congress_management.dto.institution.UpdateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.UpdateStatusInstitutionRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.institution.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Get all institutions", description = "Retrieves a list of all institutions in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of institutions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<InstitutionResponse>> getAllInstitutions() {
        List<InstitutionResponse> institutions = institutionService.getAllInstitutions()
                .stream()
                .map(InstitutionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(institutions);
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Create a new institution", description = "Creates a new institution in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Institution created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<InstitutionResponse> createInstitution(@RequestBody CreateInstitutionRequest request) throws DuplicatedEntityException {
        InstitutionResponse institution = institutionService.createInstitution(request);
        return ResponseEntity.status(201).body(institution);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Update an institution", description = "Updates the details of an existing institution. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Institution updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Institution not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<InstitutionResponse> updateInstitution(@PathVariable Long id, @RequestBody UpdateInstitutionRequest request) throws DuplicatedEntityException, NotFoundException {
        InstitutionResponse institution = institutionService.updateInstitution(id, request);
        return ResponseEntity.ok(institution);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Update institution status", description = "Updates the active status of an institution. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Institution status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Institution not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> updateInstitutionStatus(@PathVariable Long id, @RequestBody UpdateStatusInstitutionRequest request) throws NotFoundException, BusinessRuleException {
        institutionService.uptateStatusInstitution(id, request);
        return ResponseEntity.ok().build();
    }
}
