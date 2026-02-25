package com.alessandro.congress_management.controllers.institutionadministrator;

import com.alessandro.congress_management.dto.institution_administrator.InstitutionNameRequest;
import com.alessandro.congress_management.dto.institution_administrator.UserInstitutionResponse;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.institution_administrator.InstitutionAdministratorService;
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
@RequestMapping("/api/v1/institution/administrators")
@Tag(name = "Institution Administrator Controller", description = "Controller for managing institution administrators")
public class InstitutionAdministratorController {
    private final InstitutionAdministratorService institutionAdministratorService;


    public InstitutionAdministratorController(InstitutionAdministratorService institutionAdministratorService) {
        this.institutionAdministratorService = institutionAdministratorService;
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get administrators by institution", description = "Retrieves a list of administrators for a specific institution. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of administrators retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    public ResponseEntity<List<UserInstitutionResponse>> getAdministratorsByInstitution(@Valid @RequestBody InstitutionNameRequest request) throws NotFoundException {
        List<UserInstitutionResponse> administrators = institutionAdministratorService.getAdministratorsByInstitution(request);
        return ResponseEntity.ok(administrators);
    }
}
