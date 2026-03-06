package com.alessandro.congress_management.services.workshopreservation;

import com.alessandro.congress_management.dto.workshopreservation.CountWorkshopReservationsResponse;
import com.alessandro.congress_management.dto.workshopreservation.WorkShopReservationDetailsResponse;
import com.alessandro.congress_management.dto.workshopreservation.WorkShopReservationResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.models.workshop_reservation.WorkshopReservationEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.workshopreservation.WorkshopReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkShopReservationServiceImplTest {


    private static final Long   ID_USER         = 1L;
    private static final Long   ID_ACTIVITY     = 10L;
    private static final Long   ID_RESERVATION  = 20L;
    private static final Long   ID_CONGRESS     = 30L;

    private static final String USER_NAME       = "Ana López";
    private static final String USER_EMAIL      = "ana@mail.com";
    private static final String ACTIVITY_NAME   = "Spring Boot Workshop";
    private static final String ACTIVITY_DESC   = "Learn Spring Boot from scratch";
    private static final String ROOM_NAME       = "Room A";
    private static final String TYPE_TALLER     = "TALLER";
    private static final String TYPE_PONENCIA   = "PONENCIA";

    private static final int    MAX_CAPACITY    = 20;


    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusDays(5);
    private static final LocalDateTime FUTURE_END   = LocalDateTime.now().plusDays(5).plusHours(2);
    private static final LocalDateTime PAST_START   = LocalDateTime.now().minusHours(1);


    @Mock private WorkshopReservationRepository    workShopReservationRepository;
    @Mock private UserRepository                   userRepository;
    @Mock private ActivityRepository               activityRepository;
    @Mock private RegistrationRepository           registrationRepository;
    @Mock private CongressAdministratorRepository  congressAdministratorRepository;
    @Mock private ActivityTypeRepository           activityTypeRepository;

    @InjectMocks
    private WorkShopReservationServiceImpl service;

    // ----------------------------- TESTS FOR RESERVE WORKSHOP -------------------

    @Test
    void testReserveWorkshop() throws Exception {
        // Arrange
        ArgumentCaptor<WorkshopReservationEntity> captor =
                ArgumentCaptor.forClass(WorkshopReservationEntity.class);

        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(workshopActivity()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        when(workShopReservationRepository.countByActivity_IdActivity(ID_ACTIVITY)).thenReturn(5);
        when(workShopReservationRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);
        when(workShopReservationRepository.save(any())).thenAnswer(inv -> savedReservation(inv.getArgument(0)));

        // Act
        WorkShopReservationResponse result = service.reserveWorkshop(ID_ACTIVITY, ID_USER);

        // Assert
        assertAll(
                () -> verify(workShopReservationRepository).save(captor.capture()),
                () -> assertEquals(ID_ACTIVITY, captor.getValue().getActivity().getIdActivity()),
                () -> assertEquals(ID_USER,     captor.getValue().getUser().getIdUser()),
                () -> assertNotNull(captor.getValue().getReservedAt()),
                () -> assertEquals(ID_RESERVATION,  result.getIdReservation()),
                () -> assertEquals(ID_ACTIVITY,     result.getIdActivity()),
                () -> assertEquals(ACTIVITY_NAME,   result.getNameActivity()),
                () -> assertEquals(USER_NAME,        result.getUserNameReserving()),
                () -> assertEquals(USER_EMAIL,       result.getUserEmailReserving())
        );
    }

    @Test
    void testReserveWorkshop_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(ID_USER)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.reserveWorkshop(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).save(any());
    }

    @Test
    void testReserveWorkshop_WhenActivityNotFound() {
        // Arrange
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.reserveWorkshop(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).save(any());
    }

    @Test
    void testReserveWorkshop_WhenActivityIsNotAWorkshop() {
        // Arrange — activity type is PONENCIA, not TALLER
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.reserveWorkshop(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).save(any());
    }

    @Test
    void testReserveWorkshop_WhenUserNotRegisteredInCongress() {
        // Arrange
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(workshopActivity()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(false);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.reserveWorkshop(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).save(any());
    }

    @Test
    void testReserveWorkshop_WhenWorkshopIsFullyBooked() {
        // Arrange — current reservations == maxCapacity
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(workshopActivity()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        when(workShopReservationRepository.countByActivity_IdActivity(ID_ACTIVITY))
                .thenReturn(MAX_CAPACITY); // exactly at limit

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.reserveWorkshop(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).save(any());
    }

    @Test
    void testReserveWorkshop_WhenUserAlreadyReserved() {
        // Arrange
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(workshopActivity()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        when(workShopReservationRepository.countByActivity_IdActivity(ID_ACTIVITY)).thenReturn(5);
        when(workShopReservationRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.reserveWorkshop(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).save(any());
    }

    @Test
    void testReserveWorkshop_WhenActivityHasAlreadyStarted() {
        // Arrange — activity startTime is in the past
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(pastWorkshopActivity()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        when(workShopReservationRepository.countByActivity_IdActivity(ID_ACTIVITY)).thenReturn(0);
        when(workShopReservationRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.reserveWorkshop(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).save(any());
    }

    //------------------------ TEST FOR CANCEL WORKSHOP RESERVATION --------------------------

    @Test
    void testCancelWorkshopReservation() throws Exception {
        // Arrange
        WorkshopReservationEntity reservation = reservationEntity();

        // findById called twice: once in validateUserIsOwnerOfReservation, once in getReservationById
        when(workShopReservationRepository.findById(ID_RESERVATION))
                .thenReturn(Optional.of(reservation));

        // Act
        service.cancelWorkshopReservation(ID_RESERVATION, ID_USER);

        // Assert
        verify(workShopReservationRepository).delete(reservation);
    }

    @Test
    void testCancelWorkshopReservation_WhenReservationNotFound() {
        // Arrange
        when(workShopReservationRepository.findById(ID_RESERVATION)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.cancelWorkshopReservation(ID_RESERVATION, ID_USER));

        verify(workShopReservationRepository, never()).delete(any());
    }

    @Test
    void testCancelWorkshopReservation_WhenUserIsNotOwner() {
        // Arrange
        Long differentUser = 99L;
        when(workShopReservationRepository.findById(ID_RESERVATION))
                .thenReturn(Optional.of(reservationEntity())); // owner is ID_USER

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.cancelWorkshopReservation(ID_RESERVATION, differentUser));

        verify(workShopReservationRepository, never()).delete(any());
    }

    @Test
    void testCancelWorkshopReservation_WhenActivityHasAlreadyStarted() {
        // Arrange — reservation points to a past activity
        WorkshopReservationEntity pastReservation = reservationEntityWithActivity(pastWorkshopActivity());
        when(workShopReservationRepository.findById(ID_RESERVATION))
                .thenReturn(Optional.of(pastReservation));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.cancelWorkshopReservation(ID_RESERVATION, ID_USER));

        verify(workShopReservationRepository, never()).delete(any());
    }

    //------------------------ TESTS FOR GET MY WORKSHOP RESERVATION ------------------------

    @Test
    void testGetMyWorkshopReservations() {
        // Arrange
        when(workShopReservationRepository.findByUser_IdUser(ID_USER))
                .thenReturn(List.of(reservationEntity()));

        // Act
        List<WorkShopReservationDetailsResponse> result = service.getMyWorkshopReservations(ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(1,             result.size()),
                () -> assertEquals(ID_RESERVATION, result.get(0).getIdReservation()),
                () -> assertEquals(ACTIVITY_NAME,  result.get(0).getActivity().getActivityName())
        );
    }

    @Test
    void testGetMyWorkshopReservations_ReturnsEmpty_WhenNoReservations() {
        // Arrange
        when(workShopReservationRepository.findByUser_IdUser(ID_USER)).thenReturn(List.of());

        // Act
        List<WorkShopReservationDetailsResponse> result = service.getMyWorkshopReservations(ID_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ------------------------- TESTS FOR COUNT WORSHOP ---------------------------

    @Test
    void testCountWorkshopReservations() throws Exception {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(workshopActivity()));
        when(workShopReservationRepository.countByActivity_IdActivity(ID_ACTIVITY)).thenReturn(8);

        // Act
        CountWorkshopReservationsResponse result = service.countWorkshopReservations(ID_ACTIVITY);

        // Assert
        assertAll(
                () -> assertEquals(ID_ACTIVITY,   result.getIdActivity()),
                () -> assertEquals(ACTIVITY_NAME, result.getNameActivity()),
                () -> assertEquals(8,              result.getCountReservations())
        );
    }

    @Test
    void testCountWorkshopReservations_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.countWorkshopReservations(ID_ACTIVITY));
    }

    //------------------------ TESTS FOR GET ALL WORKSHOP RESERVATION ------------------

    @Test
    void testGetAllWorkshopReservationsByActivity() throws Exception {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(workshopActivity()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        when(workShopReservationRepository.findByActivity_IdActivity(ID_ACTIVITY))
                .thenReturn(List.of(reservationEntity()));

        // Act
        List<WorkShopReservationResponse> result =
                service.getAllWorkshopReservationsByActivity(ID_ACTIVITY, ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(1,             result.size()),
                () -> assertEquals(ID_RESERVATION, result.get(0).getIdReservation()),
                () -> assertEquals(ACTIVITY_NAME,  result.get(0).getNameActivity()),
                () -> assertEquals(USER_NAME,       result.get(0).getUserNameReserving()),
                () -> assertEquals(USER_EMAIL,      result.get(0).getUserEmailReserving())
        );
    }

    @Test
    void testGetAllWorkshopReservationsByActivity_ReturnsEmpty_WhenNoReservations() throws Exception {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(workshopActivity()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        when(workShopReservationRepository.findByActivity_IdActivity(ID_ACTIVITY)).thenReturn(List.of());

        // Act
        List<WorkShopReservationResponse> result =
                service.getAllWorkshopReservationsByActivity(ID_ACTIVITY, ID_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllWorkshopReservationsByActivity_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getAllWorkshopReservationsByActivity(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).findByActivity_IdActivity(any());
    }

    @Test
    void testGetAllWorkshopReservationsByActivity_WhenUserIsNotAdmin() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(workshopActivity()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(false);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.getAllWorkshopReservationsByActivity(ID_ACTIVITY, ID_USER));

        verify(workShopReservationRepository, never()).findByActivity_IdActivity(any());
    }

    // ----------------------- METHODS ------------------

    private UserEntity user() {
        UserEntity user = new UserEntity();
        user.setIdUser(ID_USER);
        user.setFullName(USER_NAME);
        user.setEmail(USER_EMAIL);
        user.setIsActive(true);
        return user;
    }

    private CongressEntity congress() {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(ID_CONGRESS);
        congress.setCongressName("Tech Congress 2026");
        congress.setIsActive(true);
        return congress;
    }

    private RoomEntity room() {
        RoomEntity room = new RoomEntity();
        room.setIdRoom(1L);
        room.setRoomName(ROOM_NAME);
        return room;
    }

    private ActivityTypeEntity workshopType() {
        ActivityTypeEntity type = new ActivityTypeEntity();
        type.setIdActivityType(2);
        type.setTypeName(TYPE_TALLER);
        return type;
    }

    private ActivityTypeEntity ponenciaType() {
        ActivityTypeEntity type = new ActivityTypeEntity();
        type.setIdActivityType(1);
        type.setTypeName(TYPE_PONENCIA);
        return type;
    }

    private ActivityEntity workshopActivity() {
        ActivityEntity activity = new ActivityEntity();
        activity.setIdActivity(ID_ACTIVITY);
        activity.setActivityName(ACTIVITY_NAME);
        activity.setDescription(ACTIVITY_DESC);
        activity.setActivityType(workshopType());
        activity.setStartTime(FUTURE_START);
        activity.setEndTime(FUTURE_END);
        activity.setRoom(room());
        activity.setCongress(congress());
        activity.setMaxCapacity(MAX_CAPACITY);
        return activity;
    }

    private ActivityEntity pastWorkshopActivity() {
        ActivityEntity activity = workshopActivity();
        activity.setStartTime(PAST_START);
        return activity;
    }

    private ActivityEntity ponenciaActivity() {
        ActivityEntity activity = new ActivityEntity();
        activity.setIdActivity(ID_ACTIVITY);
        activity.setActivityName(ACTIVITY_NAME);
        activity.setDescription(ACTIVITY_DESC);
        activity.setActivityType(ponenciaType());
        activity.setStartTime(FUTURE_START);
        activity.setEndTime(FUTURE_END);
        activity.setRoom(room());
        activity.setCongress(congress());
        activity.setMaxCapacity(null);
        return activity;
    }

    private WorkshopReservationEntity reservationEntity() {
        return reservationEntityWithActivity(workshopActivity());
    }

    private WorkshopReservationEntity reservationEntityWithActivity(ActivityEntity activity) {
        WorkshopReservationEntity reservation = new WorkshopReservationEntity();
        reservation.setIdReservation(ID_RESERVATION);
        reservation.setUser(user());
        reservation.setActivity(activity);
        reservation.setReservedAt(LocalDateTime.now());
        return reservation;
    }

    private WorkshopReservationEntity savedReservation(WorkshopReservationEntity base) {
        base.setIdReservation(ID_RESERVATION);
        return base;
    }
}