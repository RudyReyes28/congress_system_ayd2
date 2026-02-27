package com.alessandro.congress_management.services.room;


import com.alessandro.congress_management.dto.congress.CreateCongressRequest;
import com.alessandro.congress_management.dto.congress.UpdateCongressRequest;
import com.alessandro.congress_management.dto.room.CreateRoomRequest;
import com.alessandro.congress_management.dto.room.RoomResponse;
import com.alessandro.congress_management.dto.room.UpdateRoomRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.room.RoomRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.congress.CongressServiceImpl;
import com.alessandro.congress_management.services.congressadministrator.CongressAdministratorService;
import com.alessandro.congress_management.services.institution_administrator.InstitutionAdministratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceImplTest {
    @Mock
    private CongressService congressService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    //---------------- TESTS CREATE ROOM -------------------
    @Test
    public void testCreateRoom_Success() throws Exception {
        // Arrange
        Long idCongress = 1L;
        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        CreateRoomRequest request = createCreateRoomRequest("Main Hall");

        RoomEntity savedRoom = createRoom(1L, "Main Hall", congress);

        when(congressService.findCongressEntityById(idCongress)).thenReturn(congress);
        when(roomRepository.existsByRoomNameAndCongress_IdCongress(request.getRoomName(), idCongress)).thenReturn(false);
        when(roomRepository.existsByRoomCodeAndCongress_IdCongress(request.getRoomCode(), idCongress)).thenReturn(false);
        when(roomRepository.save(any(RoomEntity.class))).thenReturn(savedRoom);

        ArgumentCaptor<RoomEntity> roomCaptor = ArgumentCaptor.forClass(RoomEntity.class);

        // Act
        RoomResponse response = roomService.createRoom(request, idCongress);

        // Assert
        verify(roomRepository).save(roomCaptor.capture());
        RoomEntity capturedRoom = roomCaptor.getValue();


        assertAll(
                () -> assertEquals(request.getRoomName(), capturedRoom.getRoomName()),
                () -> assertEquals(request.getRoomCode(), capturedRoom.getRoomCode()),
                () -> assertEquals(request.getCapacity(), capturedRoom.getCapacity()),
                () -> assertEquals(request.getLocation(), capturedRoom.getLocation()),
                () -> assertEquals(request.getDescription(), capturedRoom.getDescription()),
                () -> assertEquals(congress, capturedRoom.getCongress())
        );
    }

    @Test
    public void createRoom_CongressNotActive_ThrowsBusinessRuleException() throws Exception {
        // Arrange
        Long idCongress = 1L;
        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", false);
        CreateRoomRequest request = createCreateRoomRequest("Main Hall");

        when(congressService.findCongressEntityById(idCongress)).thenReturn(congress);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> { roomService.createRoom(request, idCongress);
        });

        assertEquals("Congress is not active", exception.getMessage());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    public void createRoom_RoomNameExists_ThrowsBusinessRuleException() throws Exception {
        // Arrange
        Long idCongress = 1L;
        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        CreateRoomRequest request = createCreateRoomRequest("Main Hall");

        when(congressService.findCongressEntityById(idCongress)).thenReturn(congress);
        when(roomRepository.existsByRoomNameAndCongress_IdCongress(request.getRoomName(), idCongress)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> { roomService.createRoom(request, idCongress);
        });

        assertEquals("Room name already exists in this congress", exception.getMessage());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    public void createRoom_RoomCodeExists_ThrowsBusinessRuleException() throws Exception {
        // Arrange
        Long idCongress = 1L;
        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        CreateRoomRequest request = createCreateRoomRequest("Main Hall");

        when(congressService.findCongressEntityById(idCongress)).thenReturn(congress);
        when(roomRepository.existsByRoomNameAndCongress_IdCongress(request.getRoomName(), idCongress)).thenReturn(false);
        when(roomRepository.existsByRoomCodeAndCongress_IdCongress(request.getRoomCode(), idCongress)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> { roomService.createRoom(request, idCongress);
        });

        assertEquals("Room code already exists in this congress", exception.getMessage());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    //---------------- TESTS UPDATE ROOM -------------------
    @Test
    public void testUpdateRoom_Success() throws Exception {
        // Arrange
        Long idRoom = 1L;
        Long idCongress = 1L;

        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        RoomEntity existingRoom = createRoom(idRoom, "Main Hall", congress);
        UpdateRoomRequest request = createUpdateRoomRequest("Main Hall Updated");

        RoomEntity updatedRoom = createRoom(idRoom, "Main Hall Updated", congress);
        updatedRoom.setCapacity(request.getCapacity());
        updatedRoom.setLocation(request.getLocation());
        updatedRoom.setDescription(request.getDescription());

        when(roomRepository.findById(idRoom)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.existsByRoomNameAndCongress_IdCongressAndIdRoomNot(request.getRoomName(), idCongress, idRoom)).thenReturn(false);
        when(roomRepository.existsByRoomCodeAndCongress_IdCongressAndIdRoomNot(request.getRoomCode(), idCongress, idRoom)).thenReturn(false);
        when(roomRepository.save(any(RoomEntity.class))).thenReturn(updatedRoom);

        ArgumentCaptor<RoomEntity> roomCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        // Act
        RoomResponse response = roomService.updateRoom(idRoom, request);
        // Assert
        verify(roomRepository).save(roomCaptor.capture());
        RoomEntity capturedRoom = roomCaptor.getValue();
        assertAll(
                () -> assertEquals(request.getRoomName(), capturedRoom.getRoomName()),
                () -> assertEquals(request.getRoomCode(), capturedRoom.getRoomCode()),
                () -> assertEquals(request.getCapacity(), capturedRoom.getCapacity()),
                () -> assertEquals(request.getLocation(), capturedRoom.getLocation()),
                () -> assertEquals(request.getDescription(), capturedRoom.getDescription()),
                () -> assertEquals(congress, capturedRoom.getCongress())
        );
    }

    @Test
    public void updateRoom_RoomNameExists_ThrowsBusinessRuleException() throws Exception {
        // Arrange
        Long idRoom = 1L;
        Long idCongress = 1L;

        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        RoomEntity existingRoom = createRoom(idRoom, "Main Hall", congress);
        UpdateRoomRequest request = createUpdateRoomRequest("Main Hall Updated");

        when(roomRepository.findById(idRoom)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.existsByRoomNameAndCongress_IdCongressAndIdRoomNot(request.getRoomName(), idCongress, idRoom)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> { roomService.updateRoom(idRoom, request);
        });

        assertEquals("Room name already exists in this congress", exception.getMessage());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }
    
    @Test
    public void updateRoom_RoomCodeExists_ThrowsBusinessRuleException() throws Exception {
        // Arrange
        Long idRoom = 1L;
        Long idCongress = 1L;

        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        RoomEntity existingRoom = createRoom(idRoom, "Main Hall", congress);
        UpdateRoomRequest request = createUpdateRoomRequest("Main Hall Updated");

        when(roomRepository.findById(idRoom)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.existsByRoomNameAndCongress_IdCongressAndIdRoomNot(request.getRoomName(), idCongress, idRoom)).thenReturn(false);
        when(roomRepository.existsByRoomCodeAndCongress_IdCongressAndIdRoomNot(request.getRoomCode(), idCongress, idRoom)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> { roomService.updateRoom(idRoom, request);
        });

        assertEquals("Room code already exists in this congress", exception.getMessage());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    public void updateRoom_RoomNotFound_ThrowsNotFoundException() throws Exception {
        // Arrange
        Long idRoom = 1L;
        UpdateRoomRequest request = createUpdateRoomRequest("Main Hall Updated");

        when(roomRepository.findById(idRoom)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> { roomService.updateRoom(idRoom, request);
        });
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    //---------------- TESTS DELETE ROOM -------------------
    @Test
    public void deleteRoom_Success() throws Exception {
        // Arrange
        Long idRoom = 1L;
        Long idCongress = 1L;

        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        RoomEntity existingRoom = createRoom(idRoom, "Main Hall", congress);

        when(roomRepository.findById(idRoom)).thenReturn(Optional.of(existingRoom));
        when(activityRepository.existsByRoom_IdRoom(idRoom)).thenReturn(false);

        // Act
        roomService.deleteRoom(idRoom);

        // Assert
        verify(roomRepository).delete(existingRoom);

    }

    @Test
    public void deleteRoom_RoomNotFound_ThrowsNotFoundException() throws Exception {
        // Arrange
        Long idRoom = 1L;

        when(roomRepository.findById(idRoom)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> { roomService.deleteRoom(idRoom);
        });
        verify(roomRepository, never()).delete(any(RoomEntity.class));
    }

    @Test
    public void deleteRoom_HasAssociatedActivities_ThrowsBusinessRuleException() throws Exception {
        // Arrange
        Long idRoom = 1L;
        Long idCongress = 1L;

        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        RoomEntity existingRoom = createRoom(idRoom, "Main Hall", congress);
        when(roomRepository.findById(idRoom)).thenReturn(Optional.of(existingRoom));
        when(activityRepository.existsByRoom_IdRoom(idRoom)).thenReturn(true);
        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> { roomService.deleteRoom(idRoom);
        });

        assertEquals("Cannot delete room with associated activities", exception.getMessage());
        verify(roomRepository, never()).delete(any(RoomEntity.class));
    }

    //---------------- TESTS GET ROOM BY ID -------------------
    @Test
    public void getRoomById_Success() throws Exception {
        // Arrange
        Long idRoom = 1L;
        Long idCongress = 1L;

        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        RoomEntity existingRoom = createRoom(idRoom, "Main Hall", congress);
        when(roomRepository.findById(idRoom)).thenReturn(Optional.of(existingRoom));
        // Act
        RoomEntity room = roomService.findRoomById(idRoom);
        // Assert
        assertAll(
                () -> assertEquals(existingRoom.getRoomName(), room.getRoomName()),
                () -> assertEquals(existingRoom.getRoomCode(), room.getRoomCode()),
                () -> assertEquals(existingRoom.getCapacity(), room.getCapacity()),
                () -> assertEquals(existingRoom.getLocation(), room.getLocation()),
                () -> assertEquals(existingRoom.getDescription(), room.getDescription()),
                () -> assertEquals(congress, room.getCongress())
        );
    }

    @Test
    public void getRoomById_RoomNotFound_ThrowsNotFoundException() throws Exception {
        // Arrange
        Long idRoom = 1L;

        when(roomRepository.findById(idRoom)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> { roomService.findRoomById(idRoom);
        });
    }

    //---------------- TESTS GET ALL ROOMS BY CONGRESS ID -------------------
    @Test
    public void getAllRoomsByCongressId_Success() throws Exception {
        // Arrange
        Long idCongress = 1L;
        CongressEntity congress = createCongress(idCongress, "Tech Congress 2026", true);
        RoomEntity room1 = createRoom(1L, "Main Hall", congress);
        RoomEntity room2 = createRoom(2L, "Conference Room", congress);
        when(roomRepository.getRoomsByCongress_IdCongress(idCongress)).thenReturn(List.of(room1, room2));
        // Act
        List<RoomResponse> rooms = roomService.getRoomsByCongressId(idCongress);
        // Assert
        assertEquals(2, rooms.size());
        assertAll(
                () -> assertEquals("Main Hall", rooms.get(0).getRoomName()),
                () -> assertEquals("Conference Room", rooms.get(1).getRoomName())
        );
    }




    // ---------------- HELPER METHODS -------------------

    private RoomEntity createRoom(Long id, String name, CongressEntity congress) {
        RoomEntity room = new RoomEntity();
        room.setIdRoom(id);
        room.setRoomName(name);
        room.setRoomCode("CODE" + id);
        room.setCapacity(100);
        room.setLocation("First Floor");
        room.setDescription("Description for " + name);
        room.setCongress(congress);
        return room;
    }

    private CreateRoomRequest createCreateRoomRequest(String name) {
        return new CreateRoomRequest(
                name,
                "CODE" + name.toUpperCase(),
                100,
                "First Floor",
                "Description for " + name
        );
    }

    private UpdateRoomRequest createUpdateRoomRequest(String name) {
        return new UpdateRoomRequest(
                name,
                "CODE" + name.toUpperCase(),
                150,
                "Second Floor",
                "Updated description for " + name
        );
    }


    private CongressEntity createCongress(Long id, String name, boolean isActive) {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(id);
        congress.setCongressName(name);
        congress.setDescription("Description for " + name);
        congress.setStartDate(LocalDate.of(2026, 5, 15));
        congress.setEndDate(LocalDate.of(2026, 5, 17));
        congress.setLocation("Guatemala City");
        congress.setPrice(new BigDecimal("150.00"));
        congress.setIsActive(isActive);

        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(1L);
        institution.setInstitutionName("USAC");
        institution.setIsActive(true);
        congress.setInstitution(institution);

        return congress;
    }


}
