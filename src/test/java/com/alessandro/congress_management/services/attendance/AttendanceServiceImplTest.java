package com.alessandro.congress_management.services.attendance;

import com.alessandro.congress_management.dto.attendance.AttendanceDetailsResponse;
import com.alessandro.congress_management.dto.attendance.AttendanceResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import com.alessandro.congress_management.models.attendance.ParticipationTypeEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.activitypresenter.ActivityPresenterRepository;
import com.alessandro.congress_management.repositories.attendance.AttendanceRepository;
import com.alessandro.congress_management.repositories.attendance.ParticipationTypeRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
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
public class AttendanceServiceImplTest {


    private static final Long   ID_USER         = 1L;
    private static final Long   ID_ADMIN_USER   = 2L;
    private static final Long   ID_ACTIVITY     = 10L;
    private static final Long   ID_CONGRESS     = 20L;
    private static final Long   ID_ATTENDANCE   = 30L;

    private static final String USER_NAME         = "Ana López";
    private static final String USER_EMAIL        = "ana@mail.com";
    private static final String USER_ID_NUMBER    = "1234567";
    private static final String ADMIN_NAME        = "Admin User";
    private static final String CONGRESS_NAME     = "Tech Congress 2026";
    private static final String ACTIVITY_NAME     = "Spring Boot Workshop";
    private static final String ACTIVITY_DESC     = "Learn Spring Boot";
    private static final String ROOM_NAME         = "Room A";

    private static final String TYPE_TALLER       = "TALLER";
    private static final String TYPE_PONENCIA     = "PONENCIA";
    private static final String PART_ATTENDEE     = "ATTENDEE";
    private static final String PART_PRESENTER    = "PRESENTER";
    private static final String PART_INVITED      = "INVITED_SPEAKER";


    private static final LocalDateTime ACT_START  = LocalDateTime.now().minusMinutes(10);
    private static final LocalDateTime ACT_END    = LocalDateTime.now().plusMinutes(50);


    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusHours(5);
    private static final LocalDateTime FUTURE_END   = LocalDateTime.now().plusHours(7);


    private static final LocalDateTime PAST_START   = LocalDateTime.now().minusHours(5);
    private static final LocalDateTime PAST_END     = LocalDateTime.now().minusHours(3);


    @Mock private WorkshopReservationRepository   workShopReservationRepository;
    @Mock private UserRepository                  userRepository;
    @Mock private ActivityRepository              activityRepository;
    @Mock private ActivityPresenterRepository     activityPresenterRepository;
    @Mock private CongressRepository              congressRepository;
    @Mock private RegistrationRepository          registrationRepository;
    @Mock private CongressAdministratorRepository congressAdministratorRepository;
    @Mock private ActivityTypeRepository          activityTypeRepository;
    @Mock private AttendanceRepository            attendanceRepository;
    @Mock private ParticipationTypeRepository     participationTypeRepository;

    @InjectMocks
    private AttendanceServiceImpl service;

    //----------------- TESTS RECORD ATTENDANCE -----------------

    @Test
    void testRecordAttendance_AsAttendeeInPonencia() throws Exception {
        // Arrange
        ArgumentCaptor<AttendanceEntity> captor = ArgumentCaptor.forClass(AttendanceEntity.class);

        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);
        // Not a presenter
        when(activityPresenterRepository.findByActivity_IdActivityAndUser_IdUser(ID_ACTIVITY, ID_USER))
                .thenReturn(null);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        when(participationTypeRepository.findByTypeName(PART_ATTENDEE))
                .thenReturn(Optional.of(participationType(PART_ATTENDEE)));
        when(attendanceRepository.save(any())).thenAnswer(inv -> savedAttendance(inv.getArgument(0)));

        // Act
        AttendanceResponse result = service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER);

        // Assert
        assertAll(
                () -> verify(attendanceRepository).save(captor.capture()),
                () -> assertEquals(ID_ACTIVITY,   captor.getValue().getActivity().getIdActivity()),
                () -> assertEquals(ID_USER,        captor.getValue().getUser().getIdUser()),
                () -> assertEquals(ID_ADMIN_USER,  captor.getValue().getRecordedBy().getIdUser()),
                () -> assertEquals(PART_ATTENDEE,  captor.getValue().getParticipationType().getTypeName()),
                () -> assertEquals(ID_ATTENDANCE,  result.getIdAttendance()),
                () -> assertEquals(ACTIVITY_NAME,  result.getNameActivity()),
                () -> assertEquals(USER_NAME,       result.getNameUser()),
                () -> assertEquals(PART_ATTENDEE,  result.getParticipationType())
        );
    }

    @Test
    void testRecordAttendance_AsAttendeeInTaller_WithReservation() throws Exception {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(tallerActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);
        when(activityPresenterRepository.findByActivity_IdActivityAndUser_IdUser(ID_ACTIVITY, ID_USER))
                .thenReturn(null);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        // User has a workshop reservation
        when(workShopReservationRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(true);
        when(participationTypeRepository.findByTypeName(PART_ATTENDEE))
                .thenReturn(Optional.of(participationType(PART_ATTENDEE)));
        when(attendanceRepository.save(any())).thenAnswer(inv -> savedAttendance(inv.getArgument(0)));

        // Act
        AttendanceResponse result = service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(PART_ATTENDEE, result.getParticipationType()),
                () -> verify(attendanceRepository).save(any())
        );
    }

    //----------------- TESTS RECORD ATTENDANCE AS PRESENTER ------------------

    @Test
    void testRecordAttendance_AsPresenter() throws Exception {
        // Arrange
        ActivityPresenterEntity presenterEntity = presenterEntity(false); // not invited
        ArgumentCaptor<AttendanceEntity> captor = ArgumentCaptor.forClass(AttendanceEntity.class);

        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);
        when(activityPresenterRepository.findByActivity_IdActivityAndUser_IdUser(ID_ACTIVITY, ID_USER))
                .thenReturn(presenterEntity);
        when(participationTypeRepository.findByTypeName(PART_PRESENTER))
                .thenReturn(Optional.of(participationType(PART_PRESENTER)));
        when(attendanceRepository.save(any())).thenAnswer(inv -> savedAttendance(inv.getArgument(0)));

        // Act
        AttendanceResponse result = service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER);

        // Assert
        assertAll(
                () -> verify(attendanceRepository).save(captor.capture()),
                () -> assertEquals(PART_PRESENTER, captor.getValue().getParticipationType().getTypeName()),
                () -> assertEquals(PART_PRESENTER, result.getParticipationType()),
                // Presenter path skips congress registration check
                () -> verify(registrationRepository, never())
                        .existsByUser_IdUserAndCongress_IdCongress(any(), any())
        );
    }

    @Test
    void testRecordAttendance_AsInvitedSpeaker() throws Exception {
        // Arrange
        ActivityPresenterEntity invitedPresenter = presenterEntity(true); // invited
        ArgumentCaptor<AttendanceEntity> captor  = ArgumentCaptor.forClass(AttendanceEntity.class);

        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);
        when(activityPresenterRepository.findByActivity_IdActivityAndUser_IdUser(ID_ACTIVITY, ID_USER))
                .thenReturn(invitedPresenter);
        when(participationTypeRepository.findByTypeName(PART_INVITED))
                .thenReturn(Optional.of(participationType(PART_INVITED)));
        when(attendanceRepository.save(any())).thenAnswer(inv -> savedAttendance(inv.getArgument(0)));

        // Act
        AttendanceResponse result = service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER);

        // Assert
        assertAll(
                () -> verify(attendanceRepository).save(captor.capture()),
                () -> assertEquals(PART_INVITED, captor.getValue().getParticipationType().getTypeName()),
                () -> assertEquals(PART_INVITED, result.getParticipationType())
        );
    }

    //----------------- TESTS RECORD ATTENDANCE - FAILURE SCENARIOS ------------------

    @Test
    void testRecordAttendance_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_WhenUserNotFound() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_WhenAdminNotFound() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_WhenAdminIsNotCongressAdministrator() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_WhenActivityHasNotStartedYet() {
        // Arrange — activity starts in 5 hours, outside the 30-min-before window
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(futureActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_WhenActivityEndedLongAgo() {
        // Arrange — activity ended 3 hours ago, outside the 30-min-after window
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(pastActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_WhenAttendanceAlreadyRecorded() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_WhenUserNotRegisteredInCongress() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);
        when(activityPresenterRepository.findByActivity_IdActivityAndUser_IdUser(ID_ACTIVITY, ID_USER))
                .thenReturn(null);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_WhenTallerAndUserHasNoReservation() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(tallerActivity()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(userRepository.findById(ID_ADMIN_USER)).thenReturn(Optional.of(adminUser()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);
        when(activityPresenterRepository.findByActivity_IdActivityAndUser_IdUser(ID_ACTIVITY, ID_USER))
                .thenReturn(null);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS))
                .thenReturn(true);
        // No workshop reservation
        when(workShopReservationRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER, ID_ACTIVITY))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.recordAttendance(ID_ACTIVITY, ID_ADMIN_USER, ID_USER));

        verify(attendanceRepository, never()).save(any());
    }

    //----------------- TESTS GET MY ATTENDANCE DETAILS ------------------

    @Test
    void testGetMyAttendanceDetails() {
        // Arrange
        when(attendanceRepository.findByUser_IdUser(ID_USER))
                .thenReturn(List.of(attendanceEntity()));

        // Act
        List<AttendanceDetailsResponse> result = service.getMyAttendanceDetails(ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(1,             result.size()),
                () -> assertEquals(ID_ATTENDANCE,  result.get(0).getIdAttendance()),
                () -> assertEquals(ACTIVITY_NAME,  result.get(0).getNameActivity()),
                () -> assertEquals(CONGRESS_NAME,  result.get(0).getNameCongress()),
                () -> assertEquals(USER_NAME,       result.get(0).getNameUser()),
                () -> assertEquals(USER_EMAIL,      result.get(0).getEmailUser()),
                () -> assertEquals(PART_ATTENDEE,  result.get(0).getParticipationType())
        );
    }

    @Test
    void testGetMyAttendanceDetails_ReturnsEmpty_WhenNoAttendances() {
        // Arrange
        when(attendanceRepository.findByUser_IdUser(ID_USER)).thenReturn(List.of());

        // Act
        List<AttendanceDetailsResponse> result = service.getMyAttendanceDetails(ID_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    //------------------- TESTS GET ATTENDANCE DETAILS BY ACTIVITY ------------------

    @Test
    void testGetAttendanceDetailsByActivity() throws Exception {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_IdActivity(ID_ACTIVITY))
                .thenReturn(List.of(attendanceEntity()));

        // Act
        List<AttendanceDetailsResponse> result =
                service.getAttendanceDetailsByActivity(ID_ACTIVITY, ID_ADMIN_USER);

        // Assert
        assertAll(
                () -> assertEquals(1,            result.size()),
                () -> assertEquals(ACTIVITY_NAME, result.get(0).getNameActivity())
        );
    }

    @Test
    void testGetAttendanceDetailsByActivity_ReturnsEmpty_WhenNoAttendances() throws Exception {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_IdActivity(ID_ACTIVITY)).thenReturn(List.of());

        // Act
        List<AttendanceDetailsResponse> result =
                service.getAttendanceDetailsByActivity(ID_ACTIVITY, ID_ADMIN_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAttendanceDetailsByActivity_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getAttendanceDetailsByActivity(ID_ACTIVITY, ID_ADMIN_USER));

        verify(attendanceRepository, never()).findByActivity_IdActivity(any());
    }

    @Test
    void testGetAttendanceDetailsByActivity_WhenAdminIsNotCongressAdministrator() {
        // Arrange
        when(activityRepository.findById(ID_ACTIVITY)).thenReturn(Optional.of(ponenciaActivity()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getAttendanceDetailsByActivity(ID_ACTIVITY, ID_ADMIN_USER));

        verify(attendanceRepository, never()).findByActivity_IdActivity(any());
    }

    //------------------- TESTS GET ATTENDANCE DETAILS BY CONGRESS ------------------

    @Test
    void testGetAttendanceDetailsByCongress() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(attendanceEntity()));

        // Act
        List<AttendanceDetailsResponse> result =
                service.getAttendanceDetailsByCongress(ID_CONGRESS, ID_ADMIN_USER);

        // Assert
        assertAll(
                () -> assertEquals(1,            result.size()),
                () -> assertEquals(CONGRESS_NAME, result.get(0).getNameCongress())
        );
    }

    @Test
    void testGetAttendanceDetailsByCongress_ReturnsEmpty_WhenNoAttendances() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS)).thenReturn(List.of());

        // Act
        List<AttendanceDetailsResponse> result =
                service.getAttendanceDetailsByCongress(ID_CONGRESS, ID_ADMIN_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAttendanceDetailsByCongress_WhenCongressNotFound() {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getAttendanceDetailsByCongress(ID_CONGRESS, ID_ADMIN_USER));

        verify(attendanceRepository, never()).findByActivity_Congress_IdCongress(any());
    }

    @Test
    void testGetAttendanceDetailsByCongress_WhenAdminIsNotCongressAdministrator() {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN_USER, ID_CONGRESS))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getAttendanceDetailsByCongress(ID_CONGRESS, ID_ADMIN_USER));

        verify(attendanceRepository, never()).findByActivity_Congress_IdCongress(any());
    }

    //----------------- HELPER METHODS TO BUILD ENTITIES -----------------

    private CongressEntity congress() {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(ID_CONGRESS);
        congress.setCongressName(CONGRESS_NAME);
        congress.setIsActive(true);
        return congress;
    }

    private UserEntity user() {
        UserEntity user = new UserEntity();
        user.setIdUser(ID_USER);
        user.setFullName(USER_NAME);
        user.setEmail(USER_EMAIL);
        user.setIdentificationNumber(USER_ID_NUMBER);
        user.setIsActive(true);
        return user;
    }

    private UserEntity adminUser() {
        UserEntity admin = new UserEntity();
        admin.setIdUser(ID_ADMIN_USER);
        admin.setFullName(ADMIN_NAME);
        admin.setEmail("admin@mail.com");
        admin.setIsActive(true);
        return admin;
    }

    private RoomEntity room() {
        RoomEntity room = new RoomEntity();
        room.setIdRoom(1L);
        room.setRoomName(ROOM_NAME);
        return room;
    }

    private ActivityTypeEntity ponenciaType() {
        ActivityTypeEntity type = new ActivityTypeEntity();
        type.setIdActivityType(1);
        type.setTypeName(TYPE_PONENCIA);
        return type;
    }

    private ActivityTypeEntity tallerType() {
        ActivityTypeEntity type = new ActivityTypeEntity();
        type.setIdActivityType(2);
        type.setTypeName(TYPE_TALLER);
        return type;
    }

    private ActivityEntity buildActivity(ActivityTypeEntity type, LocalDateTime start, LocalDateTime end) {
        ActivityEntity activity = new ActivityEntity();
        activity.setIdActivity(ID_ACTIVITY);
        activity.setActivityName(ACTIVITY_NAME);
        activity.setDescription(ACTIVITY_DESC);
        activity.setActivityType(type);
        activity.setStartTime(start);
        activity.setEndTime(end);
        activity.setRoom(room());
        activity.setCongress(congress());
        activity.setMaxCapacity(20);
        return activity;
    }

    private ActivityEntity ponenciaActivity() {
        return buildActivity(ponenciaType(), ACT_START, ACT_END);
    }

    private ActivityEntity tallerActivity() {
        return buildActivity(tallerType(), ACT_START, ACT_END);
    }

    private ActivityEntity futureActivity() {
        return buildActivity(ponenciaType(), FUTURE_START, FUTURE_END);
    }

    private ActivityEntity pastActivity() {
        return buildActivity(ponenciaType(), PAST_START, PAST_END);
    }

    private ParticipationTypeEntity participationType(String name) {
        ParticipationTypeEntity type = new ParticipationTypeEntity();
        type.setIdParticipationType(1);
        type.setTypeName(name);
        return type;
    }

    private ActivityPresenterEntity presenterEntity(boolean isInvited) {
        ActivityPresenterEntity presenter = new ActivityPresenterEntity();
        presenter.setIdActivityPresenter(1L);
        presenter.setActivity(ponenciaActivity());
        presenter.setUser(user());
        presenter.setIsInvitedSpeaker(isInvited);
        presenter.setIsMainAuthor(true);
        return presenter;
    }

    private AttendanceEntity attendanceEntity() {
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setIdAttendance(ID_ATTENDANCE);
        attendance.setActivity(ponenciaActivity());
        attendance.setUser(user());
        attendance.setParticipationType(participationType(PART_ATTENDEE));
        attendance.setRecordedBy(adminUser());
        attendance.setRecordedAt(LocalDateTime.now());
        return attendance;
    }

    private AttendanceEntity savedAttendance(AttendanceEntity base) {
        base.setIdAttendance(ID_ATTENDANCE);
        base.setRecordedAt(LocalDateTime.now());
        return base;
    }
}