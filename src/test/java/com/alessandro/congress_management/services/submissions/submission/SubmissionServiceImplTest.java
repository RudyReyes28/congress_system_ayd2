package com.alessandro.congress_management.services.submissions.submission;

import com.alessandro.congress_management.dto.submissions.submission.SubmissionDetails;
import com.alessandro.congress_management.dto.submissions.submission.SubmissionRequest;
import com.alessandro.congress_management.dto.submissions.submission.SubmissionResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionStatusEntity;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.submissions.callforpapers.CallForPapersRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionStatusRepository;
import com.alessandro.congress_management.services.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceImplTest {


    private static final Long   ID_CALL          = 1L;
    private static final Long   ID_USER          = 10L;
    private static final Long   ID_CONGRESS      = 5L;
    private static final Long   ID_SUBMISSION    = 100L;
    private static final Integer ID_ACTIVITY_TYPE = 1;

    private static final String TITLE            = "Machine Learning in Healthcare";
    private static final String ABSTRACT_TEXT    = "This paper explores...";
    private static final String TITLE_UPDATED    = "Deep Learning in Healthcare";
    private static final String ABSTRACT_UPDATED = "Updated abstract text...";
    private static final String FILE_URL_OLD     = "https://bucket.s3.us-east-1.amazonaws.com/submissions/old.pdf";
    private static final String FILE_URL_NEW     = "https://bucket.s3.us-east-1.amazonaws.com/submissions/new.pdf";
    private static final String CALL_NAME        = "Call for Papers 2026";
    private static final String CONGRESS_NAME    = "Tech Congress 2026";
    private static final String STATUS_PENDING   = "PENDING";
    private static final String STATUS_REJECTED  = "REJECTED";
    private static final String TYPE_PONENCIA    = "PONENCIA";


    @Mock private SubmissionRepository       submissionRepository;
    @Mock private CallForPapersRepository    callForPapersRepository;
    @Mock private UserRepository             userRepository;
    @Mock private ActivityTypeRepository     activityTypeRepository;
    @Mock private SubmissionStatusRepository submissionStatusRepository;
    @Mock private FileStorageService         fileStorageService;
    @Mock private RegistrationRepository     registrationRepository;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    // ----------------- TESTS FOR SUBMIT ---------------------------

    @Test
    void testSubmitWithFile() throws Exception {
        // Arrange
        SubmissionRequest request  = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        MockMultipartFile file     = validPdf();
        ArgumentCaptor<SubmissionEntity> captor = ArgumentCaptor.forClass(SubmissionEntity.class);

        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(activeUser()));
        when(activityTypeRepository.findById(ID_ACTIVITY_TYPE)).thenReturn(Optional.of(activityType()));
        when(submissionStatusRepository.findByStatusName(STATUS_PENDING)).thenReturn(Optional.of(pendingStatus()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS)).thenReturn(true);
        when(submissionRepository.existsByUser_IdUserAndCallForPapers_IdCall(ID_USER, ID_CALL)).thenReturn(false);
        when(fileStorageService.uploadFile(file, "submissions")).thenReturn(FILE_URL_NEW);
        when(submissionRepository.save(any())).thenAnswer(inv -> savedEntity(inv.getArgument(0), FILE_URL_NEW));

        // Act
        SubmissionResponse result = submissionService.submit(ID_CALL, ID_USER, request, file);

        // Assert
        assertAll(
                () -> verify(submissionRepository).save(captor.capture()),
                () -> assertEquals(TITLE,        captor.getValue().getSubmissionTitle()),
                () -> assertEquals(ABSTRACT_TEXT, captor.getValue().getAbstractText()),
                () -> assertEquals(FILE_URL_NEW,  captor.getValue().getFileUrl()),
                () -> assertEquals(TITLE,         result.getSubmissionTitle()),
                () -> assertEquals(FILE_URL_NEW,  result.getFileUrl()),
                () -> assertEquals(STATUS_PENDING, result.getStatusName()),
                () -> verify(fileStorageService).uploadFile(file, "submissions")
        );
    }

    @Test
    void testSubmitWithoutFile() throws Exception {
        // Arrange
        SubmissionRequest request = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);

        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(activeUser()));
        when(activityTypeRepository.findById(ID_ACTIVITY_TYPE)).thenReturn(Optional.of(activityType()));
        when(submissionStatusRepository.findByStatusName(STATUS_PENDING)).thenReturn(Optional.of(pendingStatus()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS)).thenReturn(true);
        when(submissionRepository.existsByUser_IdUserAndCallForPapers_IdCall(ID_USER, ID_CALL)).thenReturn(false);
        when(submissionRepository.save(any())).thenAnswer(inv -> savedEntity(inv.getArgument(0), null));

        // Act
        SubmissionResponse result = submissionService.submit(ID_CALL, ID_USER, request, null);

        // Assert
        assertAll(
                () -> assertNull(result.getFileUrl()),
                () -> verifyNoInteractions(fileStorageService)
        );
    }

    @Test
    void testSubmitWhenCallNotFound() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> submissionService.submit(ID_CALL, ID_USER, request, null));
    }

    @Test
    void testSubmitWhenCallIsClosed() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(closedCall()));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> submissionService.submit(ID_CALL, ID_USER, request, null));
    }

    @Test
    void testSubmitWhenUserNotFound() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> submissionService.submit(ID_CALL, ID_USER, request, null));
    }

    @Test
    void testSubmitWhenUserNotRegisteredInCongress() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(activeUser()));
        when(activityTypeRepository.findById(ID_ACTIVITY_TYPE)).thenReturn(Optional.of(activityType()));
        when(submissionStatusRepository.findByStatusName(STATUS_PENDING)).thenReturn(Optional.of(pendingStatus()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS)).thenReturn(false);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> submissionService.submit(ID_CALL, ID_USER, request, null));
    }

    @Test
    void testSubmitWhenUserAlreadyHasSubmissionForCall() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(activeUser()));
        when(activityTypeRepository.findById(ID_ACTIVITY_TYPE)).thenReturn(Optional.of(activityType()));
        when(submissionStatusRepository.findByStatusName(STATUS_PENDING)).thenReturn(Optional.of(pendingStatus()));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(ID_USER, ID_CONGRESS)).thenReturn(true);
        when(submissionRepository.existsByUser_IdUserAndCallForPapers_IdCall(ID_USER, ID_CALL)).thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> submissionService.submit(ID_CALL, ID_USER, request, null));
    }

    @Test
    void testSubmitWhenActivityTypeNotFound() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(activeUser()));
        when(activityTypeRepository.findById(ID_ACTIVITY_TYPE)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> submissionService.submit(ID_CALL, ID_USER, request, null));
    }

    //---------------- TESTS FOR RESUBMIT ---------------------------

    @Test
    void testResubmitReplacesOldFileInS3() throws Exception {
        // Arrange
        SubmissionRequest request  = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE_UPDATED, ABSTRACT_UPDATED);
        MockMultipartFile newFile  = validPdf();
        ArgumentCaptor<SubmissionEntity> captor = ArgumentCaptor.forClass(SubmissionEntity.class);

        SubmissionEntity existing = rejectedSubmission(FILE_URL_OLD);
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(existing));
        when(activityTypeRepository.findById(ID_ACTIVITY_TYPE)).thenReturn(Optional.of(activityType()));
        when(submissionStatusRepository.findByStatusName(STATUS_PENDING)).thenReturn(Optional.of(pendingStatus()));
        when(fileStorageService.uploadFile(newFile, "submissions")).thenReturn(FILE_URL_NEW);
        when(submissionRepository.save(any())).thenAnswer(inv -> savedEntity(inv.getArgument(0), FILE_URL_NEW));

        // Act
        SubmissionResponse result = submissionService.resubmit(ID_SUBMISSION, ID_USER, request, newFile);

        // Assert
        assertAll(
                () -> verify(fileStorageService).deleteFile(FILE_URL_OLD),
                () -> verify(fileStorageService).uploadFile(newFile, "submissions"),
                () -> verify(submissionRepository).save(captor.capture()),
                () -> assertEquals(TITLE_UPDATED,    captor.getValue().getSubmissionTitle()),
                () -> assertEquals(ABSTRACT_UPDATED, captor.getValue().getAbstractText()),
                () -> assertEquals(FILE_URL_NEW,     captor.getValue().getFileUrl()),
                () -> assertEquals(STATUS_PENDING,   result.getStatusName())
        );
    }

    @Test
    void testResubmitWithoutNewFileDoesNotTouchS3() throws Exception {
        // Arrange
        SubmissionRequest request  = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE_UPDATED, ABSTRACT_UPDATED);
        SubmissionEntity existing  = rejectedSubmission(null);

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(existing));
        when(activityTypeRepository.findById(ID_ACTIVITY_TYPE)).thenReturn(Optional.of(activityType()));
        when(submissionStatusRepository.findByStatusName(STATUS_PENDING)).thenReturn(Optional.of(pendingStatus()));
        when(submissionRepository.save(any())).thenAnswer(inv -> savedEntity(inv.getArgument(0), null));

        // Act
        submissionService.resubmit(ID_SUBMISSION, ID_USER, request, null);

        // Assert
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void testResubmitWhenSubmissionNotFound() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> submissionService.resubmit(ID_SUBMISSION, ID_USER, request, null));
    }

    @Test
    void testResubmitWhenUserIsNotTheAuthor() {
        // Arrange
        SubmissionRequest request  = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        Long differentUser         = 99L;
        SubmissionEntity existing  = rejectedSubmission(null); // owner is ID_USER (10L)

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(existing));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> submissionService.resubmit(ID_SUBMISSION, differentUser, request, null));
    }

    @Test
    void testResubmitWhenSubmissionIsNotRejected() {
        // Arrange
        SubmissionRequest request  = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        SubmissionEntity existing  = pendingSubmission(); // status = PENDING

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(existing));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> submissionService.resubmit(ID_SUBMISSION, ID_USER, request, null));
    }

    @Test
    void testResubmitWhenCallIsClosed() {
        // Arrange
        SubmissionRequest request  = new SubmissionRequest(ID_ACTIVITY_TYPE, TITLE, ABSTRACT_TEXT);
        SubmissionEntity existing  = rejectedSubmissionWithClosedCall();

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(existing));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> submissionService.resubmit(ID_SUBMISSION, ID_USER, request, null));
    }

    //---------------- TESTS FOR GET SUBMISSIONS BY CALL ---------------------------

    @Test
    void testGetSubmissionsByCall() throws Exception {
        // Arrange
        when(callForPapersRepository.existsById(ID_CALL)).thenReturn(true);
        when(submissionRepository.findByCallForPapers_IdCall(ID_CALL))
                .thenReturn(List.of(fullSubmissionEntity(STATUS_PENDING)));

        // Act
        List<SubmissionResponse> result = submissionService.getSubmissionsByCall(ID_CALL);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(TITLE, result.get(0).getSubmissionTitle()),
                () -> assertEquals(STATUS_PENDING, result.get(0).getStatusName())
        );
    }

    @Test
    void testGetSubmissionsByCallReturnsEmptyList() throws Exception {
        // Arrange
        when(callForPapersRepository.existsById(ID_CALL)).thenReturn(true);
        when(submissionRepository.findByCallForPapers_IdCall(ID_CALL)).thenReturn(List.of());

        // Act
        List<SubmissionResponse> result = submissionService.getSubmissionsByCall(ID_CALL);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSubmissionsByCallWhenCallNotFound() {
        // Arrange
        when(callForPapersRepository.existsById(ID_CALL)).thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> submissionService.getSubmissionsByCall(ID_CALL));
    }

    //---------------- TESTS FOR GET SUBMISSIONS BY USER ---------------------------

    @Test
    void testGetSubmissionsByUser() throws Exception {
        // Arrange
        when(userRepository.existsById(ID_USER)).thenReturn(true);
        when(submissionRepository.findByUser_IdUser(ID_USER))
                .thenReturn(List.of(fullSubmissionEntity(STATUS_PENDING)));

        // Act
        List<SubmissionDetails> result = submissionService.getSubmissionsByUser(ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(TITLE,        result.get(0).getSubmissionTitle()),
                () -> assertEquals(CALL_NAME,    result.get(0).getCallName()),
                () -> assertEquals(CONGRESS_NAME, result.get(0).getCongressName())
        );
    }

    @Test
    void testGetSubmissionsByUserReturnsEmptyList() throws Exception {
        // Arrange
        when(userRepository.existsById(ID_USER)).thenReturn(true);
        when(submissionRepository.findByUser_IdUser(ID_USER)).thenReturn(List.of());

        // Act
        List<SubmissionDetails> result = submissionService.getSubmissionsByUser(ID_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSubmissionsByUserWhenUserNotFound() {
        // Arrange
        when(userRepository.existsById(ID_USER)).thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> submissionService.getSubmissionsByUser(ID_USER));
    }

    //----------------- HELPER METHODS ---------------------------

    private CongressEntity congress() {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(ID_CONGRESS);
        congress.setCongressName(CONGRESS_NAME);
        return congress;
    }

    private CallForPapersEntity openCall() {
        CallForPapersEntity call = new CallForPapersEntity();
        call.setIdCall(ID_CALL);
        call.setCallName(CALL_NAME);
        call.setIsOpen(true);
        call.setCongress(congress());
        return call;
    }

    private CallForPapersEntity closedCall() {
        CallForPapersEntity call = new CallForPapersEntity();
        call.setIdCall(ID_CALL);
        call.setCallName(CALL_NAME);
        call.setIsOpen(false);
        call.setCongress(congress());
        return call;
    }

    private UserEntity activeUser() {
        UserEntity user = new UserEntity();
        user.setIdUser(ID_USER);
        user.setFullName("Ana López");
        user.setIsActive(true);
        return user;
    }

    private ActivityTypeEntity activityType() {
        ActivityTypeEntity type = new ActivityTypeEntity();
        type.setIdActivityType(ID_ACTIVITY_TYPE);
        type.setTypeName(TYPE_PONENCIA);
        return type;
    }

    private SubmissionStatusEntity pendingStatus() {
        SubmissionStatusEntity status = new SubmissionStatusEntity();
        status.setIdStatus(1);
        status.setStatusName(STATUS_PENDING);
        return status;
    }

    private SubmissionStatusEntity rejectedStatus() {
        SubmissionStatusEntity status = new SubmissionStatusEntity();
        status.setIdStatus(3);
        status.setStatusName(STATUS_REJECTED);
        return status;
    }

    private SubmissionEntity rejectedSubmission(String fileUrl) {
        SubmissionEntity entity = new SubmissionEntity();
        entity.setIdSubmission(ID_SUBMISSION);
        entity.setUser(activeUser());
        entity.setCallForPapers(openCall());
        entity.setActivityType(activityType());
        entity.setSubmissionStatus(rejectedStatus());
        entity.setSubmissionTitle(TITLE);
        entity.setAbstractText(ABSTRACT_TEXT);
        entity.setFileUrl(fileUrl);
        return entity;
    }

    private SubmissionEntity rejectedSubmissionWithClosedCall() {
        SubmissionEntity entity = new SubmissionEntity();
        entity.setIdSubmission(ID_SUBMISSION);
        entity.setUser(activeUser());
        entity.setCallForPapers(closedCall());
        entity.setActivityType(activityType());
        entity.setSubmissionStatus(rejectedStatus());
        entity.setSubmissionTitle(TITLE);
        entity.setAbstractText(ABSTRACT_TEXT);
        return entity;
    }

    private SubmissionEntity pendingSubmission() {
        SubmissionEntity entity = new SubmissionEntity();
        entity.setIdSubmission(ID_SUBMISSION);
        entity.setUser(activeUser());
        entity.setCallForPapers(openCall());
        entity.setActivityType(activityType());
        entity.setSubmissionStatus(pendingStatus());
        entity.setSubmissionTitle(TITLE);
        entity.setAbstractText(ABSTRACT_TEXT);
        return entity;
    }

    private SubmissionEntity fullSubmissionEntity(String statusName) {
        SubmissionStatusEntity status = statusName.equals(STATUS_PENDING) ? pendingStatus() : rejectedStatus();
        SubmissionEntity entity = new SubmissionEntity();
        entity.setIdSubmission(ID_SUBMISSION);
        entity.setUser(activeUser());
        entity.setCallForPapers(openCall());
        entity.setActivityType(activityType());
        entity.setSubmissionStatus(status);
        entity.setSubmissionTitle(TITLE);
        entity.setAbstractText(ABSTRACT_TEXT);
        entity.setFileUrl(FILE_URL_NEW);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    private SubmissionEntity savedEntity(SubmissionEntity base, String fileUrl) {
        base.setIdSubmission(ID_SUBMISSION);
        base.setFileUrl(fileUrl);
        base.setSubmittedAt(LocalDateTime.now());
        base.setUpdatedAt(LocalDateTime.now());
        return base;
    }

    private MockMultipartFile validPdf() {
        return new MockMultipartFile("file", "paper.pdf", "application/pdf", "pdf-content".getBytes());
    }
}