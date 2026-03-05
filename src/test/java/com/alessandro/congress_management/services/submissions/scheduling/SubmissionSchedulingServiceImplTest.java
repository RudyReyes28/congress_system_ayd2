package com.alessandro.congress_management.services.submissions.scheduling;

import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.activitypresenter.ActivityPresenterRequest;
import com.alessandro.congress_management.dto.submissions.scheduling.EvaluationSubmissionDetails;
import com.alessandro.congress_management.dto.submissions.scheduling.ScheduleSubmissionsRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEvaluationEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionStatusEntity;
import com.alessandro.congress_management.repositories.submissions.evaluation.SubmissionEvaluationRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionStatusRepository;
import com.alessandro.congress_management.services.activity.ActivityService;
import com.alessandro.congress_management.services.activitypresenter.ActivityPresenterService;
import com.alessandro.congress_management.services.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubmissionSchedulingServiceImplTest {


    private static final Long   ID_SUBMISSION    = 1L;
    private static final Long   ID_ACTIVITY      = 50L;
    private static final Long   ID_CONGRESS      = 10L;
    private static final Long   ID_USER          = 20L;
    private static final Long   ID_CALL          = 30L;
    private static final Long   ID_EVALUATION    = 40L;
    private static final Long   ID_ROOM          = 5L;

    private static final String SUBMISSION_TITLE = "Neural Networks in Medicine";
    private static final String ABSTRACT_TEXT    = "This paper covers...";
    private static final String CONGRESS_NAME    = "Tech Congress 2026";
    private static final String TYPE_PONENCIA    = "PONENCIA";
    private static final String USER_NAME        = "Ana López";
    private static final String USER_EMAIL       = "ana@mail.com";
    private static final String EVALUATOR_NAME   = "Dr. García";

    private static final String STATUS_APPROVED  = "APPROVED";
    private static final String STATUS_PENDING   = "PENDING";
    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final LocalDateTime START_TIME = LocalDateTime.now().plusDays(5);
    private static final LocalDateTime END_TIME   = LocalDateTime.now().plusDays(5).plusHours(2);


    @Mock private SubmissionRepository           submissionRepository;
    @Mock private SubmissionStatusRepository     submissionStatusRepository;
    @Mock private SubmissionEvaluationRepository submissionEvaluationRepository;
    @Mock private EmailService                   emailService;
    @Mock private ActivityService                activityService;
    @Mock private ActivityPresenterService       activityPresenterService;

    @InjectMocks
    private SubmissionSchedulingServiceImpl service;

    //----------------- TESTS GET EVALUATION SUBMISSIONS BY ID CALL -----------------
    @Test
    void testGetEvaluationSubmissionsByIdCall() throws Exception {
        // Arrange
        SubmissionEvaluationEntity eval = evaluationEntity();
        when(submissionEvaluationRepository.findBySubmission_CallForPapers_IdCall(ID_CALL))
                .thenReturn(List.of(eval));

        // Act
        List<EvaluationSubmissionDetails> result = service.getEvaluationSubmissionsByIdCall(ID_CALL);

        // Assert
        assertAll(
                () -> assertEquals(1,              result.size()),
                () -> assertEquals(ID_EVALUATION,  result.get(0).getIdEvaluation()),
                () -> assertEquals(EVALUATOR_NAME, result.get(0).getNameEvaluator()),
                () -> assertTrue(result.get(0).getApproved()),
                () -> assertEquals(SUBMISSION_TITLE, result.get(0).getSubmissionDetails().getTitle())
        );
    }

    @Test
    void testGetEvaluationSubmissionsByIdCall_ReturnsEmpty_WhenNoEvaluations() throws Exception {
        // Arrange
        when(submissionEvaluationRepository.findBySubmission_CallForPapers_IdCall(ID_CALL))
                .thenReturn(List.of());

        // Act
        List<EvaluationSubmissionDetails> result = service.getEvaluationSubmissionsByIdCall(ID_CALL);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ------------------ TESTS SCHEDULE SUBMISSIONS -----------------

    @Test
    void testScheduleSubmissions_CreatesActivityAndAssignsPresenterAndSavesStatus() throws Exception {
        // Arrange
        ScheduleSubmissionsRequest request     = scheduleRequest();
        SubmissionEntity           submission  = approvedSubmission();
        ActivityResponse           actResponse = activityResponse();
        ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        ArgumentCaptor<ActivityPresenterRequest> presenterCaptor = ArgumentCaptor.forClass(ActivityPresenterRequest.class);

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(submission));
        when(activityService.createActivity(eq(ID_CONGRESS), any())).thenReturn(actResponse);
        when(activityPresenterService.assignExistingUser(any(), eq(ID_ACTIVITY))).thenReturn(null);
        when(submissionStatusRepository.findByStatusName(STATUS_SCHEDULED)).thenReturn(Optional.of(scheduledStatus()));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ActivityResponse result = service.scheduleSubmissions(ID_SUBMISSION, request);

        // Assert
        assertAll(
                () -> verify(activityService).createActivity(eq(ID_CONGRESS), any()),
                () -> verify(activityPresenterService).assignExistingUser(presenterCaptor.capture(), eq(ID_ACTIVITY)),
                () -> assertEquals(ID_USER,    presenterCaptor.getValue().getIdUser()),
                () -> assertFalse(presenterCaptor.getValue().getInvitedSpeaker()),
                () -> verify(submissionRepository).save(submissionCaptor.capture()),
                () -> assertEquals(STATUS_SCHEDULED, submissionCaptor.getValue().getSubmissionStatus().getStatusName()),
                () -> assertEquals(ID_ACTIVITY,   result.getIdActivity()),
                () -> assertEquals(SUBMISSION_TITLE, result.getActivityName())
        );
    }

    @Test
    void testScheduleSubmissions_SendsEmailAfterScheduling() throws Exception {
        // Arrange
        ScheduleSubmissionsRequest request    = scheduleRequest();
        ActivityResponse           actResponse = activityResponse();

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(approvedSubmission()));
        when(activityService.createActivity(eq(ID_CONGRESS), any())).thenReturn(actResponse);
        when(activityPresenterService.assignExistingUser(any(), eq(ID_ACTIVITY))).thenReturn(null);
        when(submissionStatusRepository.findByStatusName(STATUS_SCHEDULED)).thenReturn(Optional.of(scheduledStatus()));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        service.scheduleSubmissions(ID_SUBMISSION, request);

        // Assert
        verify(emailService).sendPresenterAcceptedEmail(
                eq(USER_EMAIL), eq(USER_NAME), eq(CONGRESS_NAME),
                eq(SUBMISSION_TITLE), eq(TYPE_PONENCIA), eq(false)
        );
    }

    @Test
    void testScheduleSubmissions_WhenSubmissionNotFound() {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.scheduleSubmissions(ID_SUBMISSION, scheduleRequest()));

        verifyNoInteractions(activityService, activityPresenterService, emailService);
    }

    @Test
    void testScheduleSubmissions_WhenSubmissionIsNotApproved() {
        // Arrange
        SubmissionEntity pending = submissionWithStatus(pendingStatus());
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(pending));

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.scheduleSubmissions(ID_SUBMISSION, scheduleRequest()));

        verifyNoInteractions(activityService, activityPresenterService, emailService);
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void testScheduleSubmissions_WhenActivityCreationFails_DoesNotSaveOrEmail() throws Exception {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(approvedSubmission()));
        when(activityService.createActivity(eq(ID_CONGRESS), any()))
                .thenThrow(new BusinessRuleException("Room conflict"));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.scheduleSubmissions(ID_SUBMISSION, scheduleRequest()));

        verify(submissionRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void testScheduleSubmissions_WhenPresenterAssignmentFails_DoesNotSaveOrEmail() throws Exception {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(approvedSubmission()));
        when(activityService.createActivity(eq(ID_CONGRESS), any())).thenReturn(activityResponse());
        when(activityPresenterService.assignExistingUser(any(), any()))
                .thenThrow(new BusinessRuleException("User already assigned"));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.scheduleSubmissions(ID_SUBMISSION, scheduleRequest()));

        verify(submissionRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void testScheduleSubmissions_WhenScheduledStatusNotFound() throws Exception {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(approvedSubmission()));
        when(activityService.createActivity(eq(ID_CONGRESS), any())).thenReturn(activityResponse());
        when(activityPresenterService.assignExistingUser(any(), eq(ID_ACTIVITY))).thenReturn(null);
        when(submissionStatusRepository.findByStatusName(STATUS_SCHEDULED)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.scheduleSubmissions(ID_SUBMISSION, scheduleRequest()));

        verify(submissionRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    //------------------- TESTS CANCEL SCHEDULING -----------------

    @Test
    void testCancelScheduling_UpdatesStatusAndSendsEmail() throws Exception {
        // Arrange
        ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(approvedSubmission()));
        when(submissionStatusRepository.findByStatusName(STATUS_CANCELLED)).thenReturn(Optional.of(cancelledStatus()));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        service.cancelScheduling(ID_SUBMISSION);

        // Assert
        assertAll(
                () -> verify(submissionRepository).save(submissionCaptor.capture()),
                () -> assertEquals(STATUS_CANCELLED, submissionCaptor.getValue().getSubmissionStatus().getStatusName()),
                () -> verify(emailService).sendSubmissionCancelledEmail(
                        eq(USER_EMAIL), eq(USER_NAME), eq(CONGRESS_NAME),
                        eq(SUBMISSION_TITLE), eq(TYPE_PONENCIA))
        );
    }

    @Test
    void testCancelScheduling_WhenSubmissionNotFound() {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.cancelScheduling(ID_SUBMISSION));

        verify(submissionRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void testCancelScheduling_WhenSubmissionIsNotApproved() {
        // Arrange
        SubmissionEntity pending = submissionWithStatus(pendingStatus());
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(pending));

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.cancelScheduling(ID_SUBMISSION));

        verify(submissionRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void testCancelScheduling_WhenCancelledStatusNotFound() {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(approvedSubmission()));
        when(submissionStatusRepository.findByStatusName(STATUS_CANCELLED)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.cancelScheduling(ID_SUBMISSION));

        verify(submissionRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    //------------------- HELPER METHODS -------------------

    private CongressEntity congress() {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(ID_CONGRESS);
        congress.setCongressName(CONGRESS_NAME);
        congress.setStartDate(LocalDate.now().minusDays(1));
        congress.setEndDate(LocalDate.now().plusDays(60));
        congress.setIsActive(true);
        return congress;
    }

    private UserEntity user() {
        UserEntity user = new UserEntity();
        user.setIdUser(ID_USER);
        user.setFullName(USER_NAME);
        user.setEmail(USER_EMAIL);
        user.setIsActive(true);
        return user;
    }

    private UserEntity evaluator() {
        UserEntity user = new UserEntity();
        user.setIdUser(99L);
        user.setFullName(EVALUATOR_NAME);
        user.setEmail("garcia@mail.com");
        return user;
    }

    private ActivityTypeEntity activityType() {
        ActivityTypeEntity type = new ActivityTypeEntity();
        type.setIdActivityType(1);
        type.setTypeName(TYPE_PONENCIA);
        return type;
    }

    private CallForPapersEntity call() {
        CallForPapersEntity call = new CallForPapersEntity();
        call.setIdCall(ID_CALL);
        call.setIsOpen(true);
        call.setCongress(congress());
        return call;
    }

    private SubmissionStatusEntity approvedStatus() {
        SubmissionStatusEntity s = new SubmissionStatusEntity();
        s.setIdStatus(2);
        s.setStatusName(STATUS_APPROVED);
        return s;
    }

    private SubmissionStatusEntity pendingStatus() {
        SubmissionStatusEntity s = new SubmissionStatusEntity();
        s.setIdStatus(1);
        s.setStatusName(STATUS_PENDING);
        return s;
    }

    private SubmissionStatusEntity scheduledStatus() {
        SubmissionStatusEntity s = new SubmissionStatusEntity();
        s.setIdStatus(4);
        s.setStatusName(STATUS_SCHEDULED);
        return s;
    }

    private SubmissionStatusEntity cancelledStatus() {
        SubmissionStatusEntity s = new SubmissionStatusEntity();
        s.setIdStatus(5);
        s.setStatusName(STATUS_CANCELLED);
        return s;
    }

    private SubmissionEntity submissionWithStatus(SubmissionStatusEntity status) {
        SubmissionEntity submission = new SubmissionEntity();
        submission.setIdSubmission(ID_SUBMISSION);
        submission.setUser(user());
        submission.setCallForPapers(call());
        submission.setActivityType(activityType());
        submission.setSubmissionStatus(status);
        submission.setSubmissionTitle(SUBMISSION_TITLE);
        submission.setAbstractText(ABSTRACT_TEXT);
        submission.setSubmittedAt(LocalDateTime.now());
        return submission;
    }

    private SubmissionEntity approvedSubmission() {
        return submissionWithStatus(approvedStatus());
    }

    private SubmissionEvaluationEntity evaluationEntity() {
        SubmissionEvaluationEntity eval = new SubmissionEvaluationEntity();
        eval.setIdEvaluation(ID_EVALUATION);
        eval.setEvaluator(evaluator());
        eval.setSubmission(approvedSubmission());
        eval.setComments("Excellent research.");
        eval.setIsApproved(true);
        eval.setEvaluatedAt(LocalDateTime.now());
        return eval;
    }

    private ActivityResponse activityResponse() {
        return new ActivityResponse(
                ID_ACTIVITY, SUBMISSION_TITLE, ABSTRACT_TEXT,
                TYPE_PONENCIA, START_TIME.toString(), END_TIME.toString(),
                "Room A", null
        );
    }

    private ScheduleSubmissionsRequest scheduleRequest() {
        return new ScheduleSubmissionsRequest(ID_ROOM, START_TIME, END_TIME, null);
    }
}