package com.alessandro.congress_management.services.room;

import com.alessandro.congress_management.dto.room.CreateRoomRequest;
import com.alessandro.congress_management.dto.room.RoomResponse;
import com.alessandro.congress_management.dto.room.UpdateRoomRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;

import java.util.List;

public interface RoomService {

    RoomResponse createRoom(CreateRoomRequest request, Long idCongress) throws BusinessRuleException, NotFoundException;

    RoomResponse updateRoom(Long idRoom, UpdateRoomRequest request) throws BusinessRuleException, NotFoundException;

    void deleteRoom(Long idRoom) throws NotFoundException, BusinessRuleException;

    RoomEntity findRoomById(Long idRoom) throws NotFoundException;

    List<RoomResponse> getRoomsByCongressId(Long idCongress);

}
