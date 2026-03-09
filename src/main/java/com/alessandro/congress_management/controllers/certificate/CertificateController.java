package com.alessandro.congress_management.controllers.certificate;

import com.alessandro.congress_management.dto.certificate.CertificateResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.FileStorageException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.certificate.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CertificateController {
    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/congresses/{idCongress}/certificates/generate")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Generate certificates for a congress", description = "Generates certificates for all attendees and presenters of a congress. Only accessible by the congress admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificates generated successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission to generate certificates for this congress"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> generateCertificatesForCongress(@PathVariable Long idCongress) throws BusinessRuleException, NotFoundException, IOException, FileStorageException {
        Long idUser = SecurityUtils.getCurrentUserId();
        certificateService.generateCertificatesForCongress(idCongress, idUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/congresses/{idCongress}/certificates")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get certificates for a congress", description = "Retrieves all certificates associated with a congress. Accessible by both congress admins and regular users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission to view certificates for this congress"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CertificateResponse>> getCertificatesByCongress(@PathVariable Long idCongress) throws NotFoundException {
        List<CertificateResponse> certificates = certificateService.getCertificatesByCongress(idCongress);
        return ResponseEntity.ok(certificates);
    }

    @GetMapping("/users/certificates")
    @Operation(summary = "Get my certificates", description = "Retrieves all certificates associated with the currently authenticated user. Accessible by both congress admins and regular users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission to view their certificates"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CertificateResponse>> getMyCertificates() throws NotFoundException {
        Long idUser = SecurityUtils.getCurrentUserId();
        List<CertificateResponse> certificates = certificateService.getCertificatesByUser(idUser);
        return ResponseEntity.ok(certificates);
    }
}
