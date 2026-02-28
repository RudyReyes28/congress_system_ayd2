package com.alessandro.congress_management.controllers.room;

import com.alessandro.congress_management.dto.room.CreateRoomRequest;
import com.alessandro.congress_management.dto.room.RoomResponse;
import com.alessandro.congress_management.dto.room.UpdateRoomRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.room.RoomService;
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
@RequestMapping("/api/v1/congresses")
@Tag( name = "Room Management", description = "Endpoints for managing rooms within congresses")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    //Los rooms pueden ser consultados por cualquier usuario, no es necesario un @PreAuthorize
    @GetMapping("/{congressId}/rooms")
    @Operation(summary = "Get all rooms of a congress", description = "Retrieves a list of all rooms assigned to a specific congress. This endpoint is accessible to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of rooms retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found")
    })
    public ResponseEntity<List<RoomResponse>> getRoomsByCongressId(@PathVariable Long congressId) throws NotFoundException {
        List<RoomResponse> response = roomService.getRoomsByCongressId(congressId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{congressId}/rooms")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Create a new room for a congress", description = "Creates a new room for a specific congress. Only users with the ADMIN_CONGRESS role can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room created successfully"),
            @ApiResponse(responseCode = "404", description = "Congress not found"),
            @ApiResponse(responseCode = "400", description = "Business rule violation")
    })
    public ResponseEntity<RoomResponse> createRoom(@PathVariable Long congressId,@Valid @RequestBody CreateRoomRequest request) throws NotFoundException, BusinessRuleException {
        RoomResponse response = roomService.createRoom(request, congressId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rooms/{roomId}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Update a room", description = "Updates the details of a specific room. Only users with the ADMIN_CONGRESS role can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room updated successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "400", description = "Business rule violation")
    })
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId, @Valid @RequestBody UpdateRoomRequest request) throws NotFoundException, BusinessRuleException {
        RoomResponse response = roomService.updateRoom(roomId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/rooms/{roomId}")
    @PreAuthorize("hasRole('ADMIN_CONGRESS')")
    @Operation(summary = "Delete a room", description = "Deletes a specific room. Only users with the ADMIN_CONGRESS role can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Room deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "400", description = "Business rule violation")
    })
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) throws NotFoundException, BusinessRuleException {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

}
