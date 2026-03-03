package com.alessandro.congress_management.services.activity;
import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.activity.CreateActivityRequest;
import com.alessandro.congress_management.dto.activity.UpdateActivityRequest;
import com.alessandro.congress_management.dto.room.CreateRoomRequest;
import com.alessandro.congress_management.dto.room.UpdateRoomRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.attendance.AttendanceRepository;
import com.alessandro.congress_management.repositories.workshopreservation.WorkshopReservationRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class ActivityServiceImplTest {
    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CongressService congressService;

    @Mock
    private RoomService roomService;

    @Mock
    private WorkshopReservationRepository workShopReservationRepository;

    @Mock
    private ActivityTypeRepository activityTypeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private ActivityServiceImpl activityService;

    //----------------- TESTS CREATE ACTIVITY -------------------

    @Test
    public void testCreateActivity_Success() throws NotFoundException, BusinessRuleException {
        // Arrange
        Long congressId = 1L;
        Long roomId = 1L;
        Integer activityTypeId = 1; // TALLER
        CreateActivityRequest request = createValidCreateActivityRequest(roomId, activityTypeId);

        CongressEntity congress = createCongress(congressId, "Tech Congress", true);
        RoomEntity room = createRoom(roomId, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(activityTypeId, "TALLER");

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(roomService.findRoomById(roomId)).thenReturn(room);
        when(activityTypeRepository.findById(activityTypeId)).thenReturn(Optional.of(activityType));

        ArgumentCaptor<ActivityEntity> activityCaptor = ArgumentCaptor.forClass(ActivityEntity.class);
        when(activityRepository.save(any(ActivityEntity.class))).thenAnswer(invocation -> {
            ActivityEntity savedActivity = invocation.getArgument(0);
            savedActivity.setIdActivity(1L);
            return savedActivity;
        });

        // Act
        ActivityResponse response = activityService.createActivity(congressId, request);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1L, response.getIdActivity()),
                () -> assertEquals(request.getActivityName(), response.getActivityName()),
                () -> assertEquals(request.getDescription(), response.getDescription()),
                () -> assertEquals("TALLER", response.getActivityType()),
                () -> assertEquals(request.getStartTime().toString(), response.getStartTime()),
                () -> assertEquals(request.getEndTime().toString(), response.getEndTime()),
                () -> assertEquals(room.getRoomName(), response.getRoomName()),
                () -> assertEquals(request.getMaxCapacity(), response.getMaxCapacity())
        );

        verify(activityRepository).save(activityCaptor.capture());
        ActivityEntity capturedActivity = activityCaptor.getValue();
        assertAll(
                () -> assertEquals(request.getActivityName(), capturedActivity.getActivityName()),
                () -> assertEquals(request.getDescription(), capturedActivity.getDescription()),
                () -> assertEquals(request.getStartTime(), capturedActivity.getStartTime()),
                () -> assertEquals(request.getEndTime(), capturedActivity.getEndTime()),
                () -> assertEquals(request.getMaxCapacity(), capturedActivity.getMaxCapacity()),
                () -> assertEquals(congress, capturedActivity.getCongress()),
                () -> assertEquals(room, capturedActivity.getRoom()),
                () -> assertEquals(activityType, capturedActivity.getActivityType())
        );
    }

    @Test
    public void testCreateActivity_InactiveCongress() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        Long roomId = 1L;
        Integer activityTypeId = 1; // TALLER
        CreateActivityRequest request = createValidCreateActivityRequest(roomId, activityTypeId);

        CongressEntity congress = createCongress(congressId, "Tech Congress", false);

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.createActivity(congressId, request);
        });

        assertEquals("Not allowed to create activities for an inactive congress.", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    public void testCreateActivity_InvalidDates() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        Long roomId = 1L;
        Integer activityTypeId = 1; // TALLER
        CreateActivityRequest request = new CreateActivityRequest(
                roomId,
                activityTypeId,
                "Activity Name",
                "Activity Description",
                LocalDate.now().plusDays(2).atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay(),
                50
        );

        CongressEntity congress = createCongress(congressId, "Tech Congress", true);

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.createActivity(congressId, request);
        });

        assertEquals("The start time must be before the end time.", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void testCreateActivity_OverlappingActivity() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        Long roomId = 1L;
        Integer activityTypeId = 1; // TALLER
        CreateActivityRequest request = createValidCreateActivityRequest(roomId, activityTypeId);

        CongressEntity congress = createCongress(congressId, "Tech Congress", true);
        RoomEntity room = createRoom(roomId, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(activityTypeId, "TALLER");

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(roomService.findRoomById(roomId)).thenReturn(room);
        when(activityRepository.existsByRoom_IdRoomAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(roomId),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.createActivity(congressId, request);
        });

        assertEquals("There is already an activity scheduled in this room during the specified time.", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void testCreateActivity_WorkshopWithoutCapacity() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        Long roomId = 1L;
        Integer activityTypeId = 1; // TALLER
        CreateActivityRequest request = new CreateActivityRequest(
                roomId,
                activityTypeId,
                "Activity Name",
                "Activity Description",
                LocalDate.now().plusDays(1).atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay().plusHours(2),
                null // No max capacity
        );

        CongressEntity congress = createCongress(congressId, "Tech Congress", true);
        RoomEntity room = createRoom(roomId, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(activityTypeId, "TALLER");

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(roomService.findRoomById(roomId)).thenReturn(room);
        when(activityTypeRepository.findById(activityTypeId)).thenReturn(Optional.of(activityType));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.createActivity(congressId, request);
        });

        assertEquals("Workshops must have a maximum capacity greater than 0", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void testCreateActivity_dateActivityOutsideCongressDates() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        Long roomId = 1L;
        Integer activityTypeId = 1; // TALLER
        CreateActivityRequest request = new CreateActivityRequest(
                roomId,
                activityTypeId,
                "Activity Name",
                "Activity Description",
                LocalDate.now().plusDays(10).atStartOfDay(), // Start time after congress end date
                LocalDate.now().plusDays(11).atStartOfDay(),
                50
        );

        CongressEntity congress = createCongress(congressId, "Tech Congress", true);
        congress.setStartDate(LocalDate.now().plusDays(1));
        congress.setEndDate(LocalDate.now().plusDays(5));

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.createActivity(congressId, request);
        });

        assertEquals("Activity times must be within the congress dates (" + congress.getStartDate() + " to " + congress.getEndDate() + ").", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    //------------- TESTS UPDATE ACTIVITY -------------------
    @Test
    public void testUpdateActivity_Success() throws BusinessRuleException, NotFoundException {
        Long activityId = 1L;
        UpdateActivityRequest request = createValidUpdateActivityRequest(100);
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));
        when(activityRepository.save(any(ActivityEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        ActivityResponse response = activityService.updateActivity(activityId, request);

        //Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(activityId, response.getIdActivity()),
                () -> assertEquals(request.getActivityName(), response.getActivityName()),
                () -> assertEquals(request.getDescription(), response.getDescription()),
                () -> assertEquals("TALLER", response.getActivityType()),
                () -> assertEquals(request.getStartTime().toString(), response.getStartTime()),
                () -> assertEquals(request.getEndTime().toString(), response.getEndTime()),
                () -> assertEquals(room.getRoomName(), response.getRoomName()),
                () -> assertEquals(request.getMaxCapacity(), response.getMaxCapacity())
        );

    }

    @Test
    public void testUpdateActivity_NotFound() {
        //Arrange
        Long activityId = 1L;
        UpdateActivityRequest request = new UpdateActivityRequest(
                1L,
                "Updated Activity Name",
                "Updated Activity Description",
                LocalDate.now().plusDays(2).atStartOfDay(),
                LocalDate.now().plusDays(2).atStartOfDay().plusHours(2),
                100
        );

        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        //Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            activityService.updateActivity(activityId, request);
        });

        assertEquals("Activity not found", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    public void testUpdateActivity_InvalidDates() {
        //Arrange
        Long activityId = 1L;
        UpdateActivityRequest request = new UpdateActivityRequest(
                1L,
                "Updated Activity Name",
                "Updated Activity Description",
                LocalDate.now().plusDays(2).atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay(),
                100
        );
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));

        //Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.updateActivity(activityId, request);
        });

        assertEquals("The start time must be before the end time.", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void testUpdateActivity_OverlappingActivity() {
        // Arrange
        Long activityId = 1L;
        UpdateActivityRequest request = createValidUpdateActivityRequest(100);
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));
        when(activityRepository.existsByRoom_IdRoomAndStartTimeLessThanAndEndTimeGreaterThanAndIdActivityNot(
                eq(room.getIdRoom()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(activityId)
        )).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.updateActivity(activityId, request);
        });

        assertEquals("There is already an activity scheduled in this room during the specified time.", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void testUpdateActivity_WorkshopWithoutCapacity() {
        //Arrange
        Long activityId = 1L;
        UpdateActivityRequest request = createValidUpdateActivityRequest(null); // No max capacity
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));

        //Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.updateActivity(activityId, request);
        });

        assertEquals("Workshops must define a valid maximum capacity", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void testUpdateActivity_WorkshopWithZeroCapacity() {
        //Arrange
        Long activityId = 1L;
        UpdateActivityRequest request = createValidUpdateActivityRequest(0); // Max capacity of 0
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));

        //Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.updateActivity(activityId, request);
        });

        assertEquals("Workshops must define a valid maximum capacity", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }


    @Test
    void testUpdateActivity_existingReservationsExceedingNewCapacity() {
        //Arrange
        Long activityId = 1L;
        UpdateActivityRequest request = createValidUpdateActivityRequest(10);
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));
        when(workShopReservationRepository.countByActivity_IdActivity(activityId)).thenReturn(15);

        //Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.updateActivity(activityId, request);
        });

        assertEquals("The maximum capacity cannot be less than existing reservations (15).", exception.getMessage());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    //--------------TESTS DELETE ACTIVITY -------------------
    @Test
    public void testDeleteActivity_Success() throws NotFoundException, BusinessRuleException {
        // Arrange
        Long activityId = 1L;
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));
        when(attendanceRepository.existsByActivity_IdActivity(activityId)).thenReturn(false);
        when(workShopReservationRepository.existsByActivity_IdActivity(activityId)).thenReturn(false);

        // Act
        activityService.deleteActivity(activityId);

        // Assert
        verify(activityRepository).delete(existingActivity);
    }

    @Test
    public void testDeleteActivity_WithAttendances() throws NotFoundException {
        // Arrange
        Long activityId = 1L;
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));
        when(attendanceRepository.existsByActivity_IdActivity(activityId)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.deleteActivity(activityId);
        });

        assertEquals("Cannot delete activity with associated attendances", exception.getMessage());
        verify(activityRepository, never()).delete(any(ActivityEntity.class));
    }

    @Test
    public void testDeleteActivity_WithReservations() throws NotFoundException {
        // Arrange
        Long activityId = 1L;
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));
        when(attendanceRepository.existsByActivity_IdActivity(activityId)).thenReturn(false);
        when(workShopReservationRepository.existsByActivity_IdActivity(activityId)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            activityService.deleteActivity(activityId);
        });

        assertEquals("Cannot delete activity with associated reservations", exception.getMessage());
        verify(activityRepository, never()).delete(any(ActivityEntity.class));
    }

    //--------------TESTS GET ACTIVITY BY ID -------------------
    @Test
    public void testGetActivityById_Success() throws NotFoundException {
        // Arrange
        Long activityId = 1L;
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity existingActivity = createActivityEntity(activityId, congress, room, 1);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(existingActivity));

        // Act
        ActivityEntity result = activityService.getActivityById(activityId);

        // Assert
        assertNotNull(result);
        assertEquals(activityId, result.getIdActivity());
    }

    @Test
    public void testGetActivityById_NotFound() {
        // Arrange
        Long activityId = 1L;

        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            activityService.getActivityById(activityId);
        });

        assertEquals("Activity not found", exception.getMessage());
    }

    //--------------TESTS GET ACTIVITIES BY ROOM ID -------------------
    @Test
    public void testGetActivitiesByRoomId_Success() throws NotFoundException {
        // Arrange
        Long roomId = 1L;
        CongressEntity congress = createCongress(1L, "Tech Congress", true);
        RoomEntity room = createRoom(roomId, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity activity1 = createActivityEntity(1L, congress, room, 1);
        ActivityEntity activity2 = createActivityEntity(2L, congress, room, 1);

        when(roomService.findRoomById(roomId)).thenReturn(room);
        when(activityRepository.findByRoom_IdRoom(roomId)).thenReturn(List.of(activity1, activity2));

        // Act
        List<ActivityResponse> responses = activityService.getActivitiesByRoomId(roomId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    public void testGetActivitiesByRoomId_RoomNotFound() throws NotFoundException {
        // Arrange
        Long roomId = 1L;

        when(roomService.findRoomById(roomId)).thenThrow(new NotFoundException("Room not found"));

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            activityService.getActivitiesByRoomId(roomId);
        });

        assertEquals("Room not found", exception.getMessage());
    }

    //--------------TESTS GET ACTIVITIES BY CONGRESS ID -------------------
    @Test
    public void testGetActivitiesByCongressId_Success() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        CongressEntity congress = createCongress(congressId, "Tech Congress", true);
        RoomEntity room = createRoom(1L, "Main Hall", congress);
        ActivityTypeEntity activityType = createActivityTypeEntity(1, "TALLER");
        ActivityEntity activity1 = createActivityEntity(1L, congress, room, 1);
        ActivityEntity activity2 = createActivityEntity(2L, congress, room, 1);

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(activityRepository.findByCongress_IdCongress(congressId)).thenReturn(List.of(activity1, activity2));

        // Act
        List<ActivityResponse> responses = activityService.getActivitiesByCongressId(congressId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    public void testGetActivitiesByCongressId_CongressNotFound() throws NotFoundException{
        // Arrange
        Long congressId = 1L;

        when(congressService.findCongressEntityById(congressId)).thenThrow(new NotFoundException("Congress not found"));

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            activityService.getActivitiesByCongressId(congressId);
        });

        assertEquals("Congress not found", exception.getMessage());
    }



    // ---------------- HELPER METHODS -------------------

    private CreateActivityRequest createValidCreateActivityRequest(Long roomId, Integer activityTypeId) {
        return new CreateActivityRequest(
                roomId,
                activityTypeId,
                "Activity Name",
                "Activity Description",
                LocalDate.now().plusDays(1).atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay().plusHours(2),
                50
        );
    }

    private UpdateActivityRequest createValidUpdateActivityRequest(Integer maxCapacity) {
        return new UpdateActivityRequest(
                1L,
                "Updated Activity Name",
                "Updated Activity Description",
                LocalDate.now().plusDays(2).atStartOfDay(),
                LocalDate.now().plusDays(2).atStartOfDay().plusHours(2),
                maxCapacity
        );
    }

    private ActivityResponse createExpectedActivityResponse(Long idActivity, String roomName) {
        return new ActivityResponse(
                idActivity,
                "Activity Name",
                "Activity Description",
                "TALLER",
                LocalDate.now().plusDays(1).atStartOfDay().toString(),
                LocalDate.now().plusDays(1).atStartOfDay().plusHours(2).toString(),
                roomName,
                50
        );
    }

    private ActivityEntity createActivityEntity(Long idActivity, CongressEntity congress, RoomEntity room, Integer activityTypeId) {
        ActivityEntity activity = new ActivityEntity();
        activity.setIdActivity(idActivity);
        activity.setActivityName("Activity Name");
        activity.setDescription("Activity Description");
        activity.setStartTime(LocalDate.now().plusDays(1).atStartOfDay());
        activity.setEndTime(LocalDate.now().plusDays(1).atStartOfDay().plusHours(2));
        activity.setMaxCapacity(50);

        // Configura el tipo de actividad según el ID
        if (activityTypeId == 1) {
            activity.setActivityType(createActivityTypeEntity(1, "TALLER"));
        } else {
            activity.setActivityType(createActivityTypeEntity(activityTypeId, "PONENCIA"));
        }

        activity.setCongress(congress);
        activity.setRoom(room);

        return activity;
    }

    private ActivityTypeEntity createActivityTypeEntity(Integer id, String typeName) {
        ActivityTypeEntity activityType = new ActivityTypeEntity();
        activityType.setIdActivityType(id);
        activityType.setTypeName(typeName);
        return activityType;
    }

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


    private CongressEntity createCongress(Long id, String name, boolean isActive) {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(id);
        congress.setCongressName(name);
        congress.setDescription("Description for " + name);
        congress.setStartDate(LocalDate.of(2026, 2, 15));
        congress.setEndDate(LocalDate.of(2026, 4, 17));
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
