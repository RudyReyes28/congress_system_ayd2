package com.alessandro.congress_management.controllers.congress;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congress.CreateCongressRequest;
import com.alessandro.congress_management.dto.congress.UpdateCongressRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.congress.CongressService;
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
@Tag(name = "Congress Controller", description = "Controller for managing congresses")
public class CongressController {
    private final CongressService congressService;

    public CongressController(CongressService congressService) {
        this.congressService = congressService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Create a new congress", description = "Creates a new congress in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Congress created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<CongressResponse> createCongress(@Valid @RequestBody CreateCongressRequest request) throws BusinessRuleException, NotFoundException, DuplicatedEntityException {
        CongressResponse congressResponse = congressService.createCongress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(congressResponse);
    }

    @PutMapping("/{idCongress}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Update an existing congress", description = "Updates the details of an existing congress. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Congress updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<CongressResponse> updateCongress(@PathVariable Long idCongress, @Valid @RequestBody UpdateCongressRequest request) throws BusinessRuleException, NotFoundException {
        CongressResponse congressResponse = congressService.updateCongress(idCongress, request);
        return ResponseEntity.ok(congressResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Get all congresses", description = "Retrieves a list of all congresses in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of congresses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<CongressResponse>> getAllCongresses() {
        List<CongressResponse> congresses = congressService.getAllCongresses();
        return ResponseEntity.ok(congresses);
    }

    @GetMapping("/public")
    @Operation(summary = "Get active congresses", description = "Retrieves a list of all active congresses in the system. This endpoint is accessible to the general public.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active congresses retrieved successfully")
    })
    public ResponseEntity<List<CongressResponse>> getActiveCongresses() {
        List<CongressResponse> congresses = congressService.getActiveCongresses();
        return ResponseEntity.ok(congresses);
    }

    @GetMapping("/admin/{idUser}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get congresses by administrator", description = "Retrieves a list of congresses managed by a specific administrator. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of congresses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Administrator not found")
    })
    public ResponseEntity<List<CongressResponse>> getCongressesByAdmin(@PathVariable Long idUser) throws NotFoundException {
        List<CongressResponse> congresses = congressService.getCongressesByAdmin(idUser);
        return ResponseEntity.ok(congresses);
    }

}
