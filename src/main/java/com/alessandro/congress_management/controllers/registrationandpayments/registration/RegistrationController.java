package com.alessandro.congress_management.controllers.registrationandpayments.registration;

import com.alessandro.congress_management.dto.registrationandpayments.registration.MyCongressRegistrationDTO;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegisteredUserDTO;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegistrationRequest;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegistrationResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.registrationandpayments.registration.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/congresses")
@Tag(name = "Registration Controller", description = "Endpoints for managing congress registrations")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/{congressId}/registrations")
    @Operation(summary = "Register a user for a congress", description = "Registers a user for a specific congress, deducting the registration fee from their wallet balance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered for the congress"),
            @ApiResponse(responseCode = "400", description = "Invalid request or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Congress or user not found")
    })
    public ResponseEntity<RegistrationResponse> registerForCongress(
            @PathVariable Long congressId) throws BusinessRuleException, NotFoundException {

        Long userId = SecurityUtils.getCurrentUserId();


        RegistrationResponse response = registrationService.registerForCongress(congressId, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-registrations")
    @Operation(summary = "Get my congress registrations", description = "Retrieves a list of congresses the authenticated user is registered for.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of user's congress registrations retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user must be authenticated")
    })
    public ResponseEntity<List<MyCongressRegistrationDTO>> getMyRegistrations() {
        Long idUser = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(registrationService.getMyRegistrations(idUser));
    }

    @GetMapping("/{congressId}/registrations")
    @PreAuthorize("hasAnyRole('ADMIN_CONGRESS', 'ADMIN_SYSTEM')")
    @Operation(summary = "Get registrations for a congress", description = "Retrieves a list of users registered for a specific congress.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of registrations for the congress retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<List<RegisteredUserDTO>> getRegistrationsByCongress(@PathVariable Long congressId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByCongress(congressId));
    }


}
