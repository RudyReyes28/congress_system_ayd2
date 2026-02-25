package com.alessandro.congress_management.controllers.congressadministrator;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congressadministrator.UserCongressResponse;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.congressadministrator.CongressAdministratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Congress Administrator Controller", description = "Controller for managing congress administrators")
public class CongressAdministratorController {
    private final CongressAdministratorService congressAdministratorService;

    public CongressAdministratorController(CongressAdministratorService congressAdministratorService) {
        this.congressAdministratorService = congressAdministratorService;
    }

    @GetMapping("/users/{idUser}/congresses/administrated")
    @PreAuthorize("hasAnyRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get congresses by administrator", description = "Retrieves a list of congresses managed by a specific administrator. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of congresses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Administrator not found")
    })
    public ResponseEntity<List<CongressResponse>> getCongressesByAdmin(@PathVariable Long idUser) throws NotFoundException {
        List<CongressResponse> congresses = congressAdministratorService.getCongressesByAdministrator(idUser);
        return ResponseEntity.ok(congresses);
    }

    @GetMapping("/congresses/{idCongress}/administrators")
    @PreAuthorize("hasAnyRole('ADMIN_CONGRESS', 'ADMIN_SYSTEM')")
    @Operation(summary = "Get administrators by congress", description = "Retrieves a list of administrators for a specific congress. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of administrators retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<List<UserCongressResponse>> getAdministratorsByCongress(@PathVariable Long idCongress) throws NotFoundException {
        List<UserCongressResponse> administrators = congressAdministratorService.getAdministratorsByCongress(idCongress);
        return ResponseEntity.ok(administrators);
    }

}
