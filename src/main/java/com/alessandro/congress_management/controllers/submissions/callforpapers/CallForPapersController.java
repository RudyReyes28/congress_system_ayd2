package com.alessandro.congress_management.controllers.submissions.callforpapers;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersDetailsResponse;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersRequest;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.submissions.callforpapers.CallForPapersService;
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
@Tag(name = "Call For Papers Controller", description = "Controller for managing call for papers")
public class CallForPapersController {
    private final CallForPapersService callForPapersService;

    public CallForPapersController(CallForPapersService callForPapersService) {
        this.callForPapersService = callForPapersService;
    }


    @PostMapping("/{idCongress}/call-for-papers")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Create a new call for papers", description = "Creates a new call for papers for a specific congress. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Call for papers created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<CallForPapersResponse> createCallForPapers(@PathVariable Long idCongress, @Valid @RequestBody CallForPapersRequest request) throws NotFoundException, BusinessRuleException {
        CallForPapersResponse callForPapersResponse = callForPapersService.createCallForPapers(idCongress, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(callForPapersResponse);
    }

    @DeleteMapping("/call-for-papers/{idCall}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Delete a call for papers", description = "Deletes a specific call for papers. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Call for papers deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Call for papers not found")
    })
    public ResponseEntity<Void> deleteCallForPapers(@PathVariable Long idCall) throws NotFoundException, BusinessRuleException {
        callForPapersService.deleteCallForPapers(idCall);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/call-for-papers/{idCall}/close")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Close a call for papers", description = "Closes a specific call for papers, preventing further submissions. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Call for papers closed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Call for papers not found")
    })
    public ResponseEntity<Void> closeCallForPapers(@PathVariable Long idCall) throws NotFoundException {
        callForPapersService.closeCallForPapers(idCall);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin-congress/call-for-papers")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get all call for papers by congress", description = "Retrieves a list of all call for papers for a specific congress. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of call for papers retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<List<CallForPapersDetailsResponse>> getAllCallForPapersByAdminCongress() throws NotFoundException {
        Long idUser = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(callForPapersService.getAllCallForPapersByAdminCongress(idUser));
    }

    @GetMapping("/{idCongress}/call-for-papers/open")
    @Operation(summary = "Get open call for papers by congress", description = "Retrieves the open call for papers for a specific congress. This endpoint is accessible to the general public.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Open call for papers retrieved successfully "),
            @ApiResponse(responseCode = "404", description = "Congress or open call for papers not found")
    })
    public ResponseEntity<CallForPapersDetailsResponse> getCallForPapersOpenByCongressId(@PathVariable Long idCongress) throws NotFoundException {
        return ResponseEntity.ok(callForPapersService.getCallForPapersOpenByCongressId(idCongress));
    }

    @GetMapping("/admin-congress/eligible-congresses")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Get eligible congresses for call for papers", description = "Retrieves a list of congresses that are eligible for creating a call for papers. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of eligible congresses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Admin congress not found")
    })
    public ResponseEntity<List<CongressResponse>> elegibleCongressesForCallForPapers() throws NotFoundException {
        Long idUser = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(callForPapersService.elegibleCongressesForCallForPapers(idUser));
    }
}
