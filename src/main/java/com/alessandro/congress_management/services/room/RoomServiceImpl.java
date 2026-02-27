package com.alessandro.congress_management.services.room;

import com.alessandro.congress_management.dto.room.CreateRoomRequest;
import com.alessandro.congress_management.dto.room.RoomResponse;
import com.alessandro.congress_management.dto.room.UpdateRoomRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.room.RoomRepository;
import com.alessandro.congress_management.services.activity.ActivityService;
import com.alessandro.congress_management.services.congress.CongressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService{
    private final RoomRepository roomRepository;
    private final CongressService congressService;
    private final ActivityRepository activityRepository;

    public RoomServiceImpl(RoomRepository roomRepository, CongressService congressService, ActivityRepository activityRepository) {
        this.roomRepository = roomRepository;
        this.congressService = congressService;
        this.activityRepository = activityRepository;
    }

    @Override
    public RoomResponse createRoom(CreateRoomRequest request, Long idCongress) throws BusinessRuleException, NotFoundException {
        //Validar que el congreso exista
        CongressEntity congress = congressService.findCongressEntityById(idCongress);
        //Validar que el congreso este activo
        if (!congress.getIsActive()) {
            throw new BusinessRuleException("Congress is not active");
        }
        //Validar que el nombre de la sala no exista en el mismo congreso
        if (roomRepository.existsByRoomNameAndCongress_IdCongress(request.getRoomName(), idCongress)) {
            throw new BusinessRuleException("Room name already exists in this congress");
        }

        //Validar que el codigo de la sala no exista en el mismo congreso
        if (roomRepository.existsByRoomCodeAndCongress_IdCongress(request.getRoomCode(), idCongress)) {
            throw new BusinessRuleException("Room code already exists in this congress");
        }

        //Crear la sala
        RoomEntity room = request.toEntity(congress);
        RoomEntity savedRoom = roomRepository.save(room);
        return RoomResponse.fromEntity(savedRoom);
    }

    @Override
    public RoomResponse updateRoom(Long idRoom, UpdateRoomRequest request) throws BusinessRuleException, NotFoundException {
        //Validar que la sala exista
        RoomEntity room = findRoomById(idRoom);

        //Validar que el nombre de la sala no exista en el mismo congreso
        if (roomRepository.existsByRoomNameAndCongress_IdCongressAndIdRoomNot(request.getRoomName(), room.getCongress().getIdCongress(), idRoom)) {
            throw new BusinessRuleException("Room name already exists in this congress");
        }

        //Validar que el codigo de la sala no exista en el mismo congreso
        if (roomRepository.existsByRoomCodeAndCongress_IdCongressAndIdRoomNot(request.getRoomCode(), room.getCongress().getIdCongress(), idRoom)) {
            throw new BusinessRuleException("Room code already exists in this congress");
        }

        //Actualizar la sala
        room.setRoomName(request.getRoomName());
        room.setRoomCode(request.getRoomCode());
        room.setCapacity(request.getCapacity());
        room.setLocation(request.getLocation());
        room.setDescription(request.getDescription());
        RoomEntity updatedRoom = roomRepository.save(room);
        return RoomResponse.fromEntity(updatedRoom);
    }

    @Override
    public void deleteRoom(Long idRoom) throws NotFoundException, BusinessRuleException {
        //Validar que la sala exista
        RoomEntity room = findRoomById(idRoom);

        //Validar que la sala no tenga actividades asociadas
        if (activityRepository.existsByRoom_IdRoom(room.getIdRoom())) {
            throw new BusinessRuleException("Cannot delete room with associated activities");
        }

        //Eliminar la sala
        roomRepository.delete(room);

    }

    @Override
    public RoomEntity findRoomById(Long idRoom) throws NotFoundException {
        return roomRepository.findById(idRoom)
                .orElseThrow(() -> new NotFoundException("Room not found"));

    }

    @Override
    public List<RoomResponse> getRoomsByCongressId(Long idCongress) {
        List<RoomEntity> rooms = roomRepository.getRoomsByCongress_IdCongress(idCongress);
        return rooms.stream().map(RoomResponse::fromEntity).toList();
    }
}
