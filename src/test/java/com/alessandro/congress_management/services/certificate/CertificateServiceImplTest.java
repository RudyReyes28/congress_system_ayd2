package com.alessandro.congress_management.services.certificate;

import com.alessandro.congress_management.dto.certificate.CertificateResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import com.alessandro.congress_management.models.attendance.ParticipationTypeEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.certificates.CertificateEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activitypresenter.ActivityPresenterRepository;
import com.alessandro.congress_management.repositories.attendance.AttendanceRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.certificate.CertificateRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.services.storage.FileStorageService;
import com.alessandro.congress_management.utils.ByteArrayMultipartFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceImplTest {

      private static final Long   ID_USER_1  = 1L;
    private static final Long   ID_USER_2  = 2L;
    private static final Long   ID_ADMIN   = 99L;
    private static final Long   ID_CONGRESS   = 10L;
    private static final Long   ID_ACTIVITY_1 = 20L;
    private static final Long   ID_ACTIVITY_2  = 21L;
    private static final Long   ID_CERTIFICATE = 100L;

    private static final String USER_NAME_1  = "Ana López";
    private static final String USER_NAME_2 = "Carlos Ruiz";
    private static final String CONGRESS_NAME = "Tech Congress 2026";
    private static final String CONGRESS_DESC = "Exploring the future of software";
    private static final String ACTIVITY_NAME = "Machine Learning in Healthcare";
    private static final String ACTIVITY_DESC = "A deep dive into ML applications";
    private static final String CERT_URL = "https://bucket.s3.amazonaws.com/certificates/cert.pdf";

    private static final String CERT_ATTENDANCE   = "ATTENDANCE";
    private static final String CERT_PRESENTATION = "PRESENTATION";
    private static final String PART_ATTENDEE     = "ATTENDEE";
    private static final String PART_PRESENTER    = "PRESENTER";
    private static final String PART_INVITED      = "INVITED_SPEAKER";


    @Mock private CertificateRepository  certificateRepository;
    @Mock private CongressRepository congressRepository;
    @Mock private UserRepository   userRepository;
    @Mock private ActivityRepository  activityRepository;
    @Mock private AttendanceRepository   attendanceRepository;
    @Mock private ActivityPresenterRepository  activityPresenterRepository;
    @Mock private CongressAdministratorRepository congressAdministratorRepository;
    @Mock private FileStorageService   fileStorageService;
    @Mock private CertificatePdfBuilder  pdfBuilder;
    @Mock private ByteArrayMultipartFile   multiPartFile;

    @InjectMocks
    private CertificateServiceImpl service;

    //----------------- TESTS FOR GENERATE CERTIFICATES FOR CONGRESS ---------------------

    @Test
    void testGenerateCertificatesForCongress_GeneratesAttendanceCertForQualifiedAttendee() throws Exception {
        // Arrange — user1 has exactly 3 ATTENDEE records (minimum)
        ArgumentCaptor<CertificateEntity> captor = ArgumentCaptor.forClass(CertificateEntity.class);

        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(attendeeList(user1(), activity1(), 3));
        when(certificateRepository.existsByUser_IdUserAndCongress_IdCongressAndCertificateType(
                ID_USER_1, ID_CONGRESS, CERT_ATTENDANCE)).thenReturn(false);
        when(pdfBuilder.buildAttendanceCertificate(any(), any(), any(), any())).thenReturn(fakePdf());
        when(fileStorageService.uploadFile(any(), eq("certificates"))).thenReturn(CERT_URL);
        when(certificateRepository.save(any())).thenAnswer(inv -> saved(inv.getArgument(0)));

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                //() -> assertEquals(1, result.size()),
                () -> verify(certificateRepository).save(captor.capture()),
                () -> assertEquals(CERT_ATTENDANCE, captor.getValue().getCertificateType()),
                () -> assertNull(captor.getValue().getActivity()),
                () -> assertEquals(CERT_URL, captor.getValue().getCertificateUrl()),
                () -> verify(pdfBuilder).buildAttendanceCertificate(
                        eq(USER_NAME_1), eq(CONGRESS_NAME), eq(CONGRESS_DESC), any())
        );
    }

    @Test
    void testGenerateCertificatesForCongress_SkipsAttendeeBelowMinimum() throws Exception {
        // Arrange — user1 has only 2 records (below the 3-record minimum)
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(attendeeList(user1(), activity1(), 2));

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                //() -> assertTrue(result.isEmpty()),
                () -> verifyNoInteractions(pdfBuilder, fileStorageService),
                () -> verify(certificateRepository, never()).save(any())
        );
    }

    @Test
    void testGenerateCertificatesForCongress_SkipsAttendeeIfCertificateAlreadyExists() throws Exception {
        // Arrange — 3 records but certificate already issued
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(attendeeList(user1(), activity1(), 3));
        when(certificateRepository.existsByUser_IdUserAndCongress_IdCongressAndCertificateType(
                ID_USER_1, ID_CONGRESS, CERT_ATTENDANCE)).thenReturn(true);

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                //() -> assertTrue(result.isEmpty()),
                () -> verifyNoInteractions(pdfBuilder, fileStorageService),
                () -> verify(certificateRepository, never()).save(any())
        );
    }

    @Test
    void testGenerateCertificatesForCongress_GeneratesPresentationCertForPresenter() throws Exception {
        // Arrange
        ArgumentCaptor<CertificateEntity> captor = ArgumentCaptor.forClass(CertificateEntity.class);

        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(record(user1(), activity1(), PART_PRESENTER)));
        when(certificateRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_1))
                .thenReturn(false);
        when(pdfBuilder.buildPresentationCertificate(any(), any(), any(), any(), any(), any()))
                .thenReturn(fakePdf());
        when(fileStorageService.uploadFile(any(), eq("certificates"))).thenReturn(CERT_URL);
        when(certificateRepository.save(any())).thenAnswer(inv -> saved(inv.getArgument(0)));

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                //() -> assertEquals(1, result.size()),
                () -> verify(certificateRepository).save(captor.capture()),
                () -> assertEquals(CERT_PRESENTATION, captor.getValue().getCertificateType()),
                () -> assertEquals(ID_ACTIVITY_1, captor.getValue().getActivity().getIdActivity()),
                () -> verify(pdfBuilder).buildPresentationCertificate(
                        eq(USER_NAME_1), eq(CONGRESS_NAME), eq(CONGRESS_DESC),
                        eq(ACTIVITY_NAME), eq(ACTIVITY_DESC), any())
        );
    }

    @Test
    void testGenerateCertificatesForCongress_GeneratesPresentationCertForInvitedSpeaker() throws Exception {
        // Arrange — INVITED_SPEAKER must also get a presentation cert
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(record(user1(), activity1(), PART_INVITED)));
        when(certificateRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_1))
                .thenReturn(false);
        when(pdfBuilder.buildPresentationCertificate(any(), any(), any(), any(), any(), any()))
                .thenReturn(fakePdf());
        when(fileStorageService.uploadFile(any(), eq("certificates"))).thenReturn(CERT_URL);
        when(certificateRepository.save(any())).thenAnswer(inv -> saved(inv.getArgument(0)));

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                ()-> verify(certificateRepository).save(any()),
                () -> verify(pdfBuilder).buildPresentationCertificate(
                        eq(USER_NAME_1), eq(CONGRESS_NAME), eq(CONGRESS_DESC),
                        eq(ACTIVITY_NAME), eq(ACTIVITY_DESC), any())
        );
    }

    @Test
    void testGenerateCertificatesForCongress_SkipsPresenterIfCertificateAlreadyExists() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(record(user1(), activity1(), PART_PRESENTER)));
        when(certificateRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_1))
                .thenReturn(true);

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                //() -> assertTrue(result.isEmpty()),
                () -> verifyNoInteractions(pdfBuilder, fileStorageService),
                () -> verify(certificateRepository, never()).save(any())
        );
    }

    @Test
    void testGenerateCertificatesForCongress_PresenterInTwoActivitiesGetsTwoCerts() throws Exception {
        // Arrange — same presenter, two different activities → two separate certs
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(
                        record(user1(), activity1(), PART_PRESENTER),
                        record(user1(), activity2(), PART_PRESENTER)
                ));
        when(certificateRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_1))
                .thenReturn(false);
        when(certificateRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_2))
                .thenReturn(false);
        when(pdfBuilder.buildPresentationCertificate(any(), any(), any(), any(), any(), any()))
                .thenReturn(fakePdf());
        when(fileStorageService.uploadFile(any(), eq("certificates"))).thenReturn(CERT_URL);
        when(certificateRepository.save(any())).thenAnswer(inv -> saved(inv.getArgument(0)));

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                //() -> assertEquals(2, result.size()),
                () -> verify(pdfBuilder, times(2)).buildPresentationCertificate(any(), any(), any(), any(), any(), any()),
                () -> verify(certificateRepository, times(2)).save(any())
        );
    }

    @Test
    void testGenerateCertificatesForCongress_MixedTypes_GeneratesBothCerts() throws Exception {
        // Arrange — user1 qualifies as ATTENDEE, user2 is a PRESENTER
        List<AttendanceEntity> mixed = new ArrayList<>(attendeeList(user1(), activity1(), 3));
        mixed.add(record(user2(), activity2(), PART_PRESENTER));

        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS)).thenReturn(mixed);
        when(certificateRepository.existsByUser_IdUserAndCongress_IdCongressAndCertificateType(
                ID_USER_1, ID_CONGRESS, CERT_ATTENDANCE)).thenReturn(false);
        when(certificateRepository.existsByUser_IdUserAndActivity_IdActivity(ID_USER_2, ID_ACTIVITY_2))
                .thenReturn(false);
        when(pdfBuilder.buildAttendanceCertificate(any(), any(), any(), any())).thenReturn(fakePdf());
        when(pdfBuilder.buildPresentationCertificate(any(), any(), any(), any(), any(), any()))
                .thenReturn(fakePdf());
        when(fileStorageService.uploadFile(any(), eq("certificates"))).thenReturn(CERT_URL);
        when(certificateRepository.save(any())).thenAnswer(inv -> saved(inv.getArgument(0)));

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert — 1 attendance + 1 presentation = 2 total
        assertAll(
                //() -> assertEquals(2, result.size()),
                () -> verify(pdfBuilder, times(1)).buildAttendanceCertificate(any(), any(), any(), any()),
                () -> verify(pdfBuilder, times(1)).buildPresentationCertificate(any(), any(), any(), any(), any(), any()),
                () -> verify(certificateRepository, times(2)).save(any())
        );
    }

    @Test
    void testGenerateCertificatesForCongress_ReturnsEmptyWhenNoAttendances() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(true);
        when(attendanceRepository.findByActivity_Congress_IdCongress(ID_CONGRESS)).thenReturn(List.of());

        // Act
        service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN);

        // Assert
        assertAll(
                //() -> assertTrue(result.isEmpty()),
                () -> verifyNoInteractions(pdfBuilder, fileStorageService)
        );
    }

    @Test
    void testGenerateCertificatesForCongress_WhenCongressNotFound() {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN));

        verifyNoInteractions(pdfBuilder, fileStorageService, attendanceRepository);
    }

    @Test
    void testGenerateCertificatesForCongress_WhenUserIsNotAdmin() {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(ID_ADMIN, ID_CONGRESS))
                .thenReturn(false);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.generateCertificatesForCongress(ID_CONGRESS, ID_ADMIN));

        verifyNoInteractions(pdfBuilder, fileStorageService);
        verify(attendanceRepository, never()).findByActivity_Congress_IdCongress(any());
    }

    // --------------- TESTS FOR GENERATE ATTENDANCE CERTIFICATE (ON-DEMAND) ---------

    @Test
    void testGenerateAttendanceCertificate() throws Exception {
        // Arrange
        ArgumentCaptor<CertificateEntity> captor = ArgumentCaptor.forClass(CertificateEntity.class);

        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(certificateRepository.findByUser_IdUserAndCongress_IdCongressAndCertificateType(
                ID_USER_1, ID_CONGRESS, CERT_ATTENDANCE)).thenReturn(Optional.empty());
        when(attendanceRepository.countByUser_IdUserAndActivity_Congress_IdCongress(ID_USER_1, ID_CONGRESS))
                .thenReturn(3L);
        when(pdfBuilder.buildAttendanceCertificate(any(), any(), any(), any())).thenReturn(fakePdf());
        when(fileStorageService.uploadFile(any(), eq("certificates"))).thenReturn(CERT_URL);
        when(certificateRepository.save(any())).thenAnswer(inv -> saved(inv.getArgument(0)));

        // Act
        CertificateResponse result = service.generateAttendanceCertificate(ID_CONGRESS, ID_USER_1);

        // Assert
        assertAll(
                () -> verify(certificateRepository).save(captor.capture()),
                () -> assertEquals(CERT_ATTENDANCE, captor.getValue().getCertificateType()),
                () -> assertNull(captor.getValue().getActivity()),
                () -> assertEquals(CERT_URL, result.getCertificateUrl()),
                () -> assertEquals(CERT_ATTENDANCE, result.getCertificateType())
        );
    }

    @Test
    void testGenerateAttendanceCertificate_IdempotentReturnsExisting() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(certificateRepository.findByUser_IdUserAndCongress_IdCongressAndCertificateType(
                ID_USER_1, ID_CONGRESS, CERT_ATTENDANCE)).thenReturn(Optional.of(saved(attendanceCert())));

        // Act
        CertificateResponse result = service.generateAttendanceCertificate(ID_CONGRESS, ID_USER_1);

        // Assert
        assertAll(
                () -> assertEquals(ID_CERTIFICATE, result.getIdCertificate()),
                () -> verifyNoInteractions(pdfBuilder, fileStorageService),
                () -> verify(certificateRepository, never()).save(any())
        );
    }

    @Test
    void testGenerateAttendanceCertificate_WhenBelowMinimumAttendances() {
        // Arrange — user has 2 records, needs 3
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(certificateRepository.findByUser_IdUserAndCongress_IdCongressAndCertificateType(
                ID_USER_1, ID_CONGRESS, CERT_ATTENDANCE)).thenReturn(Optional.empty());
        when(attendanceRepository.countByUser_IdUserAndActivity_Congress_IdCongress(ID_USER_1, ID_CONGRESS))
                .thenReturn(2L);

        // Assert
        assertThrows(RuntimeException.class,
                () -> service.generateAttendanceCertificate(ID_CONGRESS, ID_USER_1));

        verifyNoInteractions(pdfBuilder, fileStorageService);
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void testGenerateAttendanceCertificate_WhenCongressNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.generateAttendanceCertificate(ID_CONGRESS, ID_USER_1));
    }

    @Test
    void testGenerateAttendanceCertificate_WhenUserNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.generateAttendanceCertificate(ID_CONGRESS, ID_USER_1));
    }

    // ----------------- TESTS FOR GENERATE PRESENTATION CERTIFICATE (ON-DEMAND) ---------

    @Test
    void testGeneratePresentationCertificate() throws Exception {
        // Arrange
        ArgumentCaptor<CertificateEntity> captor = ArgumentCaptor.forClass(CertificateEntity.class);

        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(activityRepository.findById(ID_ACTIVITY_1)).thenReturn(Optional.of(activity1()));
        when(certificateRepository.findByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_1))
                .thenReturn(Optional.empty());
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(ID_ACTIVITY_1, ID_USER_1))
                .thenReturn(true);
        when(pdfBuilder.buildPresentationCertificate(any(), any(), any(), any(), any(), any()))
                .thenReturn(fakePdf());
        when(fileStorageService.uploadFile(any(), eq("certificates"))).thenReturn(CERT_URL);
        when(certificateRepository.save(any())).thenAnswer(inv -> savedWithActivity(inv.getArgument(0)));

        // Act
        CertificateResponse result =
                service.generatePresentationCertificate(ID_CONGRESS, ID_ACTIVITY_1, ID_USER_1);

        // Assert
        assertAll(
                () -> verify(certificateRepository).save(captor.capture()),
                () -> assertEquals(CERT_PRESENTATION, captor.getValue().getCertificateType()),
                () -> assertEquals(ID_ACTIVITY_1, captor.getValue().getActivity().getIdActivity()),
                () -> assertEquals(CERT_PRESENTATION, result.getCertificateType()),
                () -> assertEquals(ACTIVITY_NAME, result.getActivityName())
        );
    }

    @Test
    void testGeneratePresentationCertificate_IdempotentReturnsExisting() throws Exception {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(activityRepository.findById(ID_ACTIVITY_1)).thenReturn(Optional.of(activity1()));
        when(certificateRepository.findByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_1))
                .thenReturn(Optional.of(savedWithActivity(presentationCert())));

        // Act
        CertificateResponse result =
                service.generatePresentationCertificate(ID_CONGRESS, ID_ACTIVITY_1, ID_USER_1);

        // Assert
        assertAll(
                () -> assertEquals(ID_CERTIFICATE, result.getIdCertificate()),
                () -> verifyNoInteractions(pdfBuilder, fileStorageService),
                () -> verify(certificateRepository, never()).save(any())
        );
    }

    /*@Test
    void testGeneratePresentationCertificate_WhenActivityDoesNotBelongToCongress() {
        // Arrange — activity points to a different congress
        ActivityEntity foreign = activity1();
        foreign.getCongress().setIdCongress(999L);

        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(activityRepository.findById(ID_ACTIVITY_1)).thenReturn(Optional.of(foreign));
        when(certificateRepository.findByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_1))
                .thenReturn(Optional.empty());

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.generatePresentationCertificate(ID_CONGRESS, ID_ACTIVITY_1, ID_USER_1));

        verifyNoInteractions(pdfBuilder, fileStorageService);
    }*/

    @Test
    void testGeneratePresentationCertificate_WhenUserWasNotPresenter() {
        // Arrange
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(activityRepository.findById(ID_ACTIVITY_1)).thenReturn(Optional.of(activity1()));
        when(certificateRepository.findByUser_IdUserAndActivity_IdActivity(ID_USER_1, ID_ACTIVITY_1))
                .thenReturn(Optional.empty());
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(ID_ACTIVITY_1, ID_USER_1))
                .thenReturn(false);

        // Assert
        assertThrows(RuntimeException.class,
                () -> service.generatePresentationCertificate(ID_CONGRESS, ID_ACTIVITY_1, ID_USER_1));

        verify(certificateRepository, never()).save(any());
    }

    @Test
    void testGeneratePresentationCertificate_WhenActivityNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(activityRepository.findById(ID_ACTIVITY_1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.generatePresentationCertificate(ID_CONGRESS, ID_ACTIVITY_1, ID_USER_1));
    }

    //------------------------ TESTS FOR GET CERTIFICATES BY USER/CONGRESS --------------------------

    @Test
    void testGetCertificatesByUser() throws Exception {
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.of(user1()));
        when(certificateRepository.findByUser_IdUser(ID_USER_1))
                .thenReturn(List.of(saved(attendanceCert()), savedWithActivity(presentationCert())));

        List<CertificateResponse> result = service.getCertificatesByUser(ID_USER_1);

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(CERT_ATTENDANCE,   result.get(0).getCertificateType()),
                () -> assertEquals(CERT_PRESENTATION, result.get(1).getCertificateType())
        );
    }

    @Test
    void testGetCertificatesByUser_WhenUserNotFound() {
        when(userRepository.findById(ID_USER_1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getCertificatesByUser(ID_USER_1));
    }

    @Test
    void testGetCertificatesByCongress() throws Exception {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.of(congress()));
        when(certificateRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(saved(attendanceCert())));

        List<CertificateResponse> result = service.getCertificatesByCongress(ID_CONGRESS);

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(CERT_ATTENDANCE, result.get(0).getCertificateType()),
                () -> assertEquals(CONGRESS_NAME,   result.get(0).getCongressName())
        );
    }

    @Test
    void testGetCertificatesByCongress_WhenCongressNotFound() {
        when(congressRepository.findById(ID_CONGRESS)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getCertificatesByCongress(ID_CONGRESS));
    }

    //------------------------ HELPER METHODS TO BUILD ENTITIES --------------------------

    private CongressEntity congress() {
        CongressEntity c = new CongressEntity();
        c.setIdCongress(ID_CONGRESS);
        c.setCongressName(CONGRESS_NAME);
        c.setDescription(CONGRESS_DESC);
        c.setIsActive(true);
        c.setStartDate(LocalDate.of(2026, 3, 10));
        c.setEndDate(LocalDate.of(2026, 3, 12));
        return c;
    }

    private UserEntity user1() {
        UserEntity u = new UserEntity();
        u.setIdUser(ID_USER_1);
        u.setFullName(USER_NAME_1);
        u.setEmail("ana@mail.com");
        u.setIsActive(true);
        return u;
    }

    private UserEntity user2() {
        UserEntity u = new UserEntity();
        u.setIdUser(ID_USER_2);
        u.setFullName(USER_NAME_2);
        u.setEmail("carlos@mail.com");
        u.setIsActive(true);
        return u;
    }

    private ActivityEntity activity1() {
        ActivityEntity a = new ActivityEntity();
        a.setIdActivity(ID_ACTIVITY_1);
        a.setActivityName(ACTIVITY_NAME);
        a.setDescription(ACTIVITY_DESC);
        a.setActivityType(actType());
        a.setRoom(room());
        a.setCongress(congress());
        return a;
    }

    private ActivityEntity activity2() {
        ActivityEntity a = new ActivityEntity();
        a.setIdActivity(ID_ACTIVITY_2);
        a.setActivityName("Cloud Architecture");
        a.setDescription("Cloud patterns and practices");
        a.setActivityType(actType());
        a.setRoom(room());
        a.setCongress(congress());
        return a;
    }

    private ActivityTypeEntity actType() {
        ActivityTypeEntity t = new ActivityTypeEntity();
        t.setIdActivityType(1);
        t.setTypeName("PONENCIA");
        return t;
    }

    private RoomEntity room() {
        RoomEntity r = new RoomEntity();
        r.setIdRoom(1L);
        r.setRoomName("Room A");
        return r;
    }

    private ParticipationTypeEntity partType(String name) {
        ParticipationTypeEntity p = new ParticipationTypeEntity();
        p.setIdParticipationType(1);
        p.setTypeName(name);
        return p;
    }

    private AttendanceEntity record(UserEntity user, ActivityEntity activity, String type) {
        AttendanceEntity a = new AttendanceEntity();
        a.setUser(user);
        a.setActivity(activity);
        a.setParticipationType(partType(type));
        a.setRecordedAt(LocalDateTime.now());
        return a;
    }

    /** Produces N ATTENDEE attendance records for the same user+activity. */
    private List<AttendanceEntity> attendeeList(UserEntity user, ActivityEntity activity, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> record(user, activity, PART_ATTENDEE))
                .toList();
    }

    private CertificateEntity attendanceCert() {
        CertificateEntity e = new CertificateEntity();
        e.setCongress(congress());
        e.setUser(user1());
        e.setCertificateType(CERT_ATTENDANCE);
        e.setActivity(null);
        e.setCertificateUrl(CERT_URL);
        return e;
    }

    private CertificateEntity presentationCert() {
        CertificateEntity e = new CertificateEntity();
        e.setCongress(congress());
        e.setUser(user1());
        e.setCertificateType(CERT_PRESENTATION);
        e.setActivity(activity1());
        e.setCertificateUrl(CERT_URL);
        return e;
    }

    private CertificateEntity saved(CertificateEntity base) {
        base.setIdCertificate(ID_CERTIFICATE);
        base.setGeneratedAt(LocalDateTime.now());
        return base;
    }

    private CertificateEntity savedWithActivity(CertificateEntity base) {
        base.setIdCertificate(ID_CERTIFICATE);
        base.setGeneratedAt(LocalDateTime.now());
        if (base.getActivity() == null) base.setActivity(activity1());
        return base;
    }

    private byte[] fakePdf() {
        return new byte[]{'%', 'P', 'D', 'F', '-', '1', '.', '4'};
    }
}