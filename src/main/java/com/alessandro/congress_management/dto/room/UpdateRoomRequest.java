package com.alessandro.congress_management.dto.room;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class UpdateRoomRequest {
    @NotBlank(message = "Room name is required")
    String roomName;
    @NotBlank(message = "Room code is required")
    String roomCode;
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    Integer capacity;
    @NotBlank(message = "Location is required")
    String location;
    @NotBlank(message = "Description is required")
    String description;
}
