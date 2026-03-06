package com.alessandro.congress_management.controllers.system_configuration;


import com.alessandro.congress_management.dto.system_configuration.SystemConfigurationResponse;
import com.alessandro.congress_management.dto.system_configuration.UpdateConfigValue;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.system_configuration.SystemConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/system-configurations")
@Tag(name = "System Configuration Controller", description = "Controller for managing system configuration")
public class SystemConfigurationController {

    private final SystemConfigurationService systemConfigurationService;

    public SystemConfigurationController(SystemConfigurationService systemConfigurationService) {
        this.systemConfigurationService = systemConfigurationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Get system configuration", description = "Retrieves the current system configuration. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System configuration retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<SystemConfigurationResponse>> getSystemConfiguration() {
        List<SystemConfigurationResponse> configurations = systemConfigurationService.getAllConfigurations()
                .stream()
                .map(SystemConfigurationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(configurations);
    }

    @PutMapping("/{configKey}")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Update system configuration", description = "Updates a specific system configuration value. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System configuration updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Configuration key not found")
    })
    public ResponseEntity<SystemConfigurationResponse> updateSystemConfiguration(@PathVariable String configKey, @Valid @RequestBody UpdateConfigValue updateConfigValue) throws BusinessRuleException, NotFoundException {
        SystemConfigurationResponse updatedConfig = systemConfigurationService.updateConfigurationConfigValue(configKey, updateConfigValue);
        return ResponseEntity.ok(updatedConfig);
    }

}
