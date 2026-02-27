package com.alessandro.congress_management.dto.room;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class CreateRoomRequest {
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

        public RoomEntity toEntity(CongressEntity congress) {
            RoomEntity room = new RoomEntity();
            room.setCongress(congress);
            room.setRoomName(this.roomName);
            room.setRoomCode(this.roomCode);
            room.setCapacity(this.capacity);
            room.setLocation(this.location);
            room.setDescription(this.description);
            return room;
        }

}
