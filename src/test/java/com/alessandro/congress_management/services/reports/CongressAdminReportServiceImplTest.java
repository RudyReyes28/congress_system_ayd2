package com.alessandro.congress_management.services.reports;

import com.alessandro.congress_management.dto.reports.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import com.alessandro.congress_management.models.attendance.ParticipationTypeEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.models.workshop_reservation.WorkshopReservationEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activitypresenter.ActivityPresenterRepository;
import com.alessandro.congress_management.repositories.attendance.AttendanceRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.system_configuration.SystemConfigurationRepository;
import com.alessandro.congress_management.repositories.workshopreservation.WorkshopReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class CongressAdminReportServiceImplTest {


    private static final Long   ID_ADMIN      = 1L;
    private static final Long   ID_USER_1     = 10L;
    private static final Long   ID_USER_2     = 11L;
    private static final Long   ID_CONGRESS   = 20L;
    private static final Long   ID_ACTIVITY_1 = 30L;
    private static final Long   ID_ACTIVITY_2 = 31L;
    private static final Long   ID_ROOM       = 40L;

    private static final String CONGRESS_NAME  = "Tech Congress 2026";
    private static final String ACTIVITY_NAME  = "Spring Boot Workshop";
    private static final String ACTIVITY_2_NAME = "ML Presentation";
    private static final String ROOM_NAME       = "Room A";
    private static final String COMMISSION      = "15";


    @Mock private CongressRepository              congressRepository;
    @Mock private CongressAdministratorRepository congressAdminRepository;
    @Mock private RegistrationRepository          registrationRepository;
    @Mock private AttendanceRepository            attendanceRepository;
    @Mock private ActivityRepository              activityRepository;
    @Mock private ActivityPresenterRepository     activityPresenterRepository;
    @Mock private WorkshopReservationRepository   workshopReservationRepository;
    @Mock private SystemConfigurationRepository   systemConfigRepository;
    @Mock private UserRepository                  userRepository;

    @InjectMocks
    private CongressAdminReportServiceImpl service;

    //-------- TESTS GET PARTICIPANTS REPORT --------
    @Test
    void testGetParticipantsReport_AllParticipants() throws Exception {
        // Arrange — user1 is only ATTENDEE, user2 is ATTENDEE + PRESENTER
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(registrationRepository.findUsersByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(user1(), user2()));
        when(activityPresenterRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(presenterEntity(user2(), false)));
        when(userRepository.findAllById(any())).thenReturn(List.of(user1(), user2()));

        // Act
        ParticipantsReportResponse result =
                service.getParticipantsReport(ID_CONGRESS, ID_ADMIN, null);

        // Assert
        assertAll(
                () -> assertEquals(2, result.getTotalParticipants()),
                () -> assertEquals(CONGRESS_NAME, result.getCongressName())
        );
    }

    @Test
    void testGetParticipantsReport_FilteredByPresenter() throws Exception {
        // Arrange — only user2 is PRESENTER
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(registrationRepository.findUsersByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(user1(), user2()));
        when(activityPresenterRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(presenterEntity(user2(), false)));
        when(userRepository.findAllById(any())).thenReturn(List.of(user1(), user2()));

        // Act
        ParticipantsReportResponse result =
                service.getParticipantsReport(ID_CONGRESS, ID_ADMIN, "PRESENTER");

        // Assert — only user2 passes the filter
        assertAll(
                () -> assertEquals(1, result.getTotalParticipants()),
                () -> assertEquals("Carlos Ruiz", result.getParticipants().get(0).getFullName())
        );
    }

    @Test
    void testGetParticipantsReport_InvitedSpeakerType() throws Exception {
        // Arrange — user2 is INVITED_SPEAKER
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(registrationRepository.findUsersByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(user1(), user2()));
        when(activityPresenterRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(presenterEntity(user2(), true)));
        when(userRepository.findAllById(any())).thenReturn(List.of(user1(), user2()));

        // Act
        ParticipantsReportResponse result =
                service.getParticipantsReport(ID_CONGRESS, ID_ADMIN, "INVITED_SPEAKER");

        // Assert
        assertAll(
                () -> assertEquals(1, result.getTotalParticipants()),
                () -> assertTrue(result.getParticipants().get(0).getParticipationTypes().contains("INVITED_SPEAKER"))
        );
    }

    @Test
    void testGetParticipantsReport_WhenCongressNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getParticipantsReport(ID_CONGRESS, ID_ADMIN, null));

        verify(registrationRepository, never()).findUsersByCongress_IdCongress(any());
    }

    @Test
    void testGetParticipantsReport_WhenUserIsNotAdmin() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(false);

        assertThrows(BusinessRuleException.class,
                () -> service.getParticipantsReport(ID_CONGRESS, ID_ADMIN, null));
    }

    //-------------- TESTS GET ACTIVITY ATTENDANCE REPORT --------------

    @Test
    void testGetActivityAttendanceReport_AllActivities() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(activityRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(activity1(), activity2()));
        when(attendanceRepository.countByActivity_IdActivity(ID_ACTIVITY_1)).thenReturn(15);
        when(attendanceRepository.countByActivity_IdActivity(ID_ACTIVITY_2)).thenReturn(8);

        // Act
        ActivityAttendanceReportResponse result =
                service.getActivityAttendanceReport(ID_CONGRESS, ID_ADMIN, null, null, null, null);

        // Assert
        assertAll(
                () -> assertEquals(2,  result.getActivities().size()),
                () -> assertEquals(23, result.getTotalAttendanceRecords()),
                () -> assertEquals(15, result.getActivities().get(0).getAttendanceCount()),
                () -> assertEquals(8,  result.getActivities().get(1).getAttendanceCount()),
                () -> assertEquals(ROOM_NAME, result.getActivities().get(0).getRoomName())
        );
    }

    @Test
    void testGetActivityAttendanceReport_FilteredByActivity() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(activityRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(activity1(), activity2()));
        when(attendanceRepository.countByActivity_IdActivity(ID_ACTIVITY_1)).thenReturn(15);

        // Act — filter to only activity1
        ActivityAttendanceReportResponse result =
                service.getActivityAttendanceReport(ID_CONGRESS, ID_ADMIN, ID_ACTIVITY_1, null, null, null);

        // Assert
        assertAll(
                () -> assertEquals(1,  result.getActivities().size()),
                () -> assertEquals(ID_ACTIVITY_1, result.getActivities().get(0).getIdActivity()),
                () -> assertEquals(15, result.getTotalAttendanceRecords())
        );
    }

    @Test
    void testGetActivityAttendanceReport_FilteredByRoom() throws Exception {
        // Arrange — both activities have same room; filter by room shows both
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(activityRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(activity1(), activity2()));
        when(attendanceRepository.countByActivity_IdActivity(ID_ACTIVITY_1)).thenReturn(5);
        when(attendanceRepository.countByActivity_IdActivity(ID_ACTIVITY_2)).thenReturn(3);

        // Act
        ActivityAttendanceReportResponse result =
                service.getActivityAttendanceReport(ID_CONGRESS, ID_ADMIN, null, ID_ROOM, null, null);

        // Assert — both activities are in ID_ROOM
        assertEquals(2, result.getActivities().size());
    }

    @Test
    void testGetActivityAttendanceReport_FilteredByDateRange() throws Exception {
        // Arrange — activity1 starts March 10, activity2 starts June 1; filter for March only
        LocalDateTime rangeStart = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime rangeEnd   = LocalDateTime.of(2026, 3, 31, 23, 59);

        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(activityRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(activity1(), activity2())); // activity2 starts June, out of range
        when(attendanceRepository.countByActivity_IdActivity(ID_ACTIVITY_1)).thenReturn(10);

        // Act
        ActivityAttendanceReportResponse result =
                service.getActivityAttendanceReport(ID_CONGRESS, ID_ADMIN, null, null, rangeStart, rangeEnd);

        // Assert — only activity1 (March) passes the date filter
        assertAll(
                () -> assertEquals(1, result.getActivities().size()),
                () -> assertEquals(ID_ACTIVITY_1, result.getActivities().get(0).getIdActivity())
        );
    }

    @Test
    void testGetActivityAttendanceReport_WhenCongressNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getActivityAttendanceReport(ID_CONGRESS, ID_ADMIN, null, null, null, null));
    }

    @Test
    void testGetActivityAttendanceReport_WhenUserIsNotAdmin() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(false);

        assertThrows(BusinessRuleException.class,
                () -> service.getActivityAttendanceReport(ID_CONGRESS, ID_ADMIN, null, null, null, null));
    }

    //------------- tests get workshop reservation report --------------

    @Test
    void testGetWorkshopReservationReport_AllWorkshops() throws Exception {
        // Arrange — activity1 is TALLER with 20 capacity, 2 reservations
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(activityRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(workshopActivity(), activity2())); // activity2 is PONENCIA → excluded
        when(workshopReservationRepository.findByActivity_IdActivity(ID_ACTIVITY_1))
                .thenReturn(List.of(
                        reservation(user1(), workshopActivity()),
                        reservation(user2(), workshopActivity())
                ));

        // Act
        WorkshopReservationReportResponse result =
                service.getWorkshopReservationReport(ID_CONGRESS, ID_ADMIN, null);

        // Assert — only the TALLER appears, with 2 reservations and 18 available
        assertAll(
                () -> assertEquals(1, result.getWorkshops().size()),
                () -> assertEquals(ACTIVITY_NAME, result.getWorkshops().get(0).getWorkshopName()),
                () -> assertEquals(20, result.getWorkshops().get(0).getTotalCapacity()),
                () -> assertEquals(2,  result.getWorkshops().get(0).getReservationCount()),
                () -> assertEquals(18, result.getWorkshops().get(0).getAvailableSpots()),
                () -> assertEquals(2,  result.getWorkshops().get(0).getReservedParticipants().size())
        );
    }

    @Test
    void testGetWorkshopReservationReport_FilteredByActivity() throws Exception {
        // Arrange — two workshops; filter to only activity1
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        ActivityEntity workshop2 = workshopActivity();
        workshop2.setIdActivity(ID_ACTIVITY_2);
        workshop2.setActivityName("Docker Workshop");
        when(activityRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(workshopActivity(), workshop2));
        when(workshopReservationRepository.findByActivity_IdActivity(ID_ACTIVITY_1))
                .thenReturn(List.of(reservation(user1(), workshopActivity())));

        // Act
        WorkshopReservationReportResponse result =
                service.getWorkshopReservationReport(ID_CONGRESS, ID_ADMIN, ID_ACTIVITY_1);

        // Assert — only activity1 passes filter
        assertAll(
                () -> assertEquals(1, result.getWorkshops().size()),
                () -> assertEquals(ID_ACTIVITY_1, result.getWorkshops().get(0).getIdActivity())
        );
    }

    @Test
    void testGetWorkshopReservationReport_WorkshopFullyBooked() throws Exception {
        // Arrange — 20/20 reserved → 0 available
        ActivityEntity fullWorkshop = workshopActivity();
        fullWorkshop.setMaxCapacity(2);

        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(activityRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(fullWorkshop));
        when(workshopReservationRepository.findByActivity_IdActivity(ID_ACTIVITY_1))
                .thenReturn(List.of(
                        reservation(user1(), fullWorkshop),
                        reservation(user2(), fullWorkshop)
                ));

        // Act
        WorkshopReservationReportResponse result =
                service.getWorkshopReservationReport(ID_CONGRESS, ID_ADMIN, null);

        // Assert
        assertAll(
                () -> assertEquals(2, result.getWorkshops().get(0).getReservationCount()),
                () -> assertEquals(0, result.getWorkshops().get(0).getAvailableSpots())
        );
    }

    @Test
    void testGetWorkshopReservationReport_WhenCongressNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getWorkshopReservationReport(ID_CONGRESS, ID_ADMIN, null));
    }

    @Test
    void testGetWorkshopReservationReport_WhenUserIsNotAdmin() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(false);

        assertThrows(BusinessRuleException.class,
                () -> service.getWorkshopReservationReport(ID_CONGRESS, ID_ADMIN, null));
    }

    //--------------------- TESTS GET CONGRESS EARNINGS REPORT ---------------------

    @Test
    void testGetCongressEarningsReport() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.of(configEntity(COMMISSION)));
        when(registrationRepository.sumAmountPaidByCongressId(ID_CONGRESS))
                .thenReturn(new BigDecimal("2000.00"));
        when(registrationRepository.countByCongress_IdCongress(ID_CONGRESS)).thenReturn(20);

        // Act
        CongressEarningsReportResponse result =
                service.getCongressEarningsReport(ID_CONGRESS, ID_ADMIN);

        // Assert: totalRevenue=2000, commission=300 (15%), net=1700
        assertAll(
                () -> assertEquals(CONGRESS_NAME,             result.getCongressName()),
                () -> assertEquals(new BigDecimal("2000.00"), result.getTotalRevenue()),
                () -> assertEquals(new BigDecimal("300.00"),  result.getCommissionAmount()),
                () -> assertEquals(new BigDecimal("1700.00"), result.getNetEarnings()),
                () -> assertEquals(20, result.getTotalRegistrations())
        );
    }

    @Test
    void testGetCongressEarningsReport_WhenNoRegistrations() throws Exception {
        // Arrange — null from SUM aggregate
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.of(configEntity(COMMISSION)));
        when(registrationRepository.sumAmountPaidByCongressId(ID_CONGRESS)).thenReturn(null);
        when(registrationRepository.countByCongress_IdCongress(ID_CONGRESS)).thenReturn(0);

        // Act
        CongressEarningsReportResponse result =
                service.getCongressEarningsReport(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                () -> assertEquals(BigDecimal.ZERO,        result.getTotalRevenue()),
                () -> assertEquals(new BigDecimal("0.00"), result.getCommissionAmount()),
                () -> assertEquals(new BigDecimal("0.00"), result.getNetEarnings())
        );
    }

    @Test
    void testGetCongressEarningsReport_WhenCongressNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getCongressEarningsReport(ID_CONGRESS, ID_ADMIN));
    }

    @Test
    void testGetCongressEarningsReport_WhenUserIsNotAdmin() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(false);

        assertThrows(BusinessRuleException.class,
                () -> service.getCongressEarningsReport(ID_CONGRESS, ID_ADMIN));
    }

    @Test
    void testGetCongressEarningsReport_WhenCommissionConfigNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getCongressEarningsReport(ID_CONGRESS, ID_ADMIN));
    }

    //-------------- METHODS --------------------

    private CongressEntity congress() {
        CongressEntity c = new CongressEntity();
        c.setIdCongress(ID_CONGRESS);
        c.setCongressName(CONGRESS_NAME);
        c.setDescription("Conference about technology");
        c.setLocation("Guatemala");
        c.setStartDate(LocalDate.of(2026, 3, 10));
        c.setEndDate(LocalDate.of(2026, 3, 12));
        c.setPrice(new BigDecimal("100.00"));
        c.setIsActive(true);
        return c;
    }

    private UserEntity user1() {
        UserEntity u = new UserEntity();
        u.setIdUser(ID_USER_1);
        u.setFullName("Ana López");
        u.setIdentificationNumber("1234567");
        u.setEmail("ana@mail.com");
        u.setPhoneNumber("555-0001");
        u.setOrganization("USAC");
        u.setIsActive(true);
        return u;
    }

    private UserEntity user2() {
        UserEntity u = new UserEntity();
        u.setIdUser(ID_USER_2);
        u.setFullName("Carlos Ruiz");
        u.setIdentificationNumber("7654321");
        u.setEmail("carlos@mail.com");
        u.setPhoneNumber("555-0002");
        u.setOrganization("URL");
        u.setIsActive(true);
        return u;
    }

    private RoomEntity room() {
        RoomEntity r = new RoomEntity();
        r.setIdRoom(ID_ROOM);
        r.setRoomName(ROOM_NAME);
        r.setCapacity(50);
        r.setIsActive(true);
        return r;
    }

    private ActivityTypeEntity ponenciaType() {
        ActivityTypeEntity t = new ActivityTypeEntity();
        t.setIdActivityType(1);
        t.setTypeName("PONENCIA");
        return t;
    }

    private ActivityTypeEntity tallerType() {
        ActivityTypeEntity t = new ActivityTypeEntity();
        t.setIdActivityType(2);
        t.setTypeName("TALLER");
        return t;
    }

    private ActivityEntity activity1() {
        ActivityEntity a = new ActivityEntity();
        a.setIdActivity(ID_ACTIVITY_1);
        a.setActivityName(ACTIVITY_NAME);
        a.setDescription("Spring Boot deep dive");
        a.setActivityType(ponenciaType());
        a.setRoom(room());
        a.setCongress(congress());
        a.setStartTime(LocalDateTime.of(2026, 3, 10, 9, 0));
        a.setEndTime(LocalDateTime.of(2026, 3, 10, 11, 0));
        a.setMaxCapacity(50);
        return a;
    }

    private ActivityEntity activity2() {
        ActivityEntity a = new ActivityEntity();
        a.setIdActivity(ID_ACTIVITY_2);
        a.setActivityName(ACTIVITY_2_NAME);
        a.setDescription("Machine learning basics");
        a.setActivityType(ponenciaType());
        a.setRoom(room());
        a.setCongress(congress());
        a.setStartTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        a.setEndTime(LocalDateTime.of(2026, 6, 1, 12, 0));
        a.setMaxCapacity(50);
        return a;
    }

    private ActivityEntity workshopActivity() {
        ActivityEntity a = new ActivityEntity();
        a.setIdActivity(ID_ACTIVITY_1);
        a.setActivityName(ACTIVITY_NAME);
        a.setDescription("Hands-on Spring Boot");
        a.setActivityType(tallerType());
        a.setRoom(room());
        a.setCongress(congress());
        a.setStartTime(LocalDateTime.of(2026, 3, 10, 9, 0));
        a.setEndTime(LocalDateTime.of(2026, 3, 10, 11, 0));
        a.setMaxCapacity(20);
        return a;
    }

    private ActivityPresenterEntity presenterEntity(UserEntity user, boolean isInvited) {
        ActivityPresenterEntity p = new ActivityPresenterEntity();
        p.setIdActivityPresenter(1L);
        p.setActivity(activity1());
        p.setUser(user);
        p.setIsInvitedSpeaker(isInvited);
        p.setIsMainAuthor(true);
        return p;
    }

    private WorkshopReservationEntity reservation(UserEntity user, ActivityEntity activity) {
        WorkshopReservationEntity r = new WorkshopReservationEntity();
        r.setIdReservation(1L);
        r.setUser(user);
        r.setActivity(activity);
        r.setReservedAt(LocalDateTime.now());
        return r;
    }

    private SystemConfigurationEntity configEntity(String value) {
        SystemConfigurationEntity cfg = new SystemConfigurationEntity();
        cfg.setConfigKey("COMMISSION_PERCENTAGE");
        cfg.setConfigValue(value);
        return cfg;
    }
}