package com.alessandro.congress_management.dto.room;

import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import lombok.Value;

@Value
public class RoomResponse {
    Long idRoom;
    String roomName;
    String roomCode;
    Integer capacity;
    String location;
    String description;
    String congressName;

    public static RoomResponse fromEntity (RoomEntity room) {
        return new RoomResponse(
                room.getIdRoom(),
                room.getRoomName(),
                room.getRoomCode(),
                room.getCapacity(),
                room.getLocation(),
                room.getDescription(),
                room.getCongress().getCongressName()
        );
    }

}
