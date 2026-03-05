package com.alessandro.congress_management.services.submissions.evaluation;

import com.alessandro.congress_management.dto.submissions.evaluation.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.congress_management.ScientificCommiteeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEvaluationEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionStatusEntity;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.scientificcommitee.ScientificCommiteeRepository;
import com.alessandro.congress_management.repositories.submissions.callforpapers.CallForPapersRepository;
import com.alessandro.congress_management.repositories.submissions.evaluation.SubmissionEvaluationRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionStatusRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EvaluationScientificCommiteeServiceImplTest {



    private static final Long    ID_USER         = 1L;
    private static final Long    ID_EVALUATOR    = 2L;
    private static final Long    ID_CONGRESS     = 10L;
    private static final Long    ID_CALL         = 20L;
    private static final Long    ID_SUBMISSION   = 30L;
    private static final Long    ID_EVALUATION   = 40L;

    private static final String  CONGRESS_NAME   = "Tech Congress 2026";
    private static final String  CALL_NAME       = "Call for Papers 2026";
    private static final String  SUBMISSION_TITLE = "My Research Paper";
    private static final String  ABSTRACT_TEXT   = "This paper covers...";
    private static final String  FILE_URL        = "https://bucket.s3.amazonaws.com/paper.pdf";
    private static final String  COMMENTS        = "Great paper, well structured.";
    private static final String  TYPE_PONENCIA   = "PONENCIA";
    private static final String  USER_NAME       = "Ana López";
    private static final String  USER_EMAIL      = "ana@mail.com";
    private static final String  EVALUATOR_NAME  = "Dr. García";

    private static final String  STATUS_PENDING  = "PENDING";
    private static final String  STATUS_APPROVED = "APPROVED";
    private static final String  STATUS_REJECTED = "REJECTED";

    private static final LocalDate     START_DATE = LocalDate.now().plusDays(5);
    private static final LocalDate     END_DATE   = LocalDate.now().plusDays(30);
    private static final LocalDateTime OPEN_DATE  = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime CLOSE_DATE = LocalDateTime.now().plusDays(20);


    @Mock private SubmissionRepository           submissionRepository;
    @Mock private CallForPapersRepository        callForPapersRepository;
    @Mock private UserRepository                 userRepository;
    @Mock private ActivityTypeRepository         activityTypeRepository;
    @Mock private SubmissionStatusRepository     submissionStatusRepository;
    @Mock private SubmissionEvaluationRepository submissionEvaluationRepository;
    @Mock private CongressRepository             congressRepository;
    @Mock private ScientificCommiteeRepository   scientificCommitteeRepository;
    @Mock private EmailService                   emailService;

    @InjectMocks
    private EvaluationScientificCommiteeServiceImpl service;

    //----------- TESTS FOR GET MY COMMITTEE CONGRESSES -----------

    @Test
    void testGetMyCommitteeCongresses() throws Exception {
        // Arrange
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(scientificCommitteeRepository.findByUser_IdUser(ID_USER))
                .thenReturn(List.of(scientificCommitteeEntity()));

        // Act
        List<CongressSummaryResponse> result = service.getMyCommitteeCongresses(ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(ID_CONGRESS,   result.get(0).getIdCongress()),
                () -> assertEquals(CONGRESS_NAME, result.get(0).getCongressName()),
                () -> assertEquals(START_DATE,    result.get(0).getStartDate()),
                () -> assertEquals(END_DATE,      result.get(0).getEndDate())
        );
    }

    @Test
    void testGetMyCommitteeCongresses_ReturnsEmpty_WhenNoCommittees() throws Exception {
        // Arrange
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        when(scientificCommitteeRepository.findByUser_IdUser(ID_USER)).thenReturn(List.of());

        // Act
        List<CongressSummaryResponse> result = service.getMyCommitteeCongresses(ID_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMyCommitteeCongresses_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(ID_USER)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getMyCommitteeCongresses(ID_USER));
    }

    //----------------------- TESTS FOR GET CALLS BY CONGRESS FOR COMMITTEE -----------------------

    @Test
    void testGetCallsByCongressForCommittee() throws Exception {
        // Arrange
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_USER))
                .thenReturn(true);
        when(callForPapersRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(openCall()));

        // Act
        List<CallSummaryResponse> result = service.getCallsByCongressForCommittee(ID_CONGRESS, ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(1,         result.size()),
                () -> assertEquals(ID_CALL,   result.get(0).getIdCall()),
                () -> assertEquals(CALL_NAME, result.get(0).getCallName()),
                () -> assertTrue(result.get(0).isOpen())
        );
    }

    @Test
    void testGetCallsByCongressForCommittee_ReturnsEmpty_WhenNoCalls() throws Exception {
        // Arrange
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_USER))
                .thenReturn(true);
        when(callForPapersRepository.findByCongress_IdCongress(ID_CONGRESS)).thenReturn(List.of());

        // Act
        List<CallSummaryResponse> result = service.getCallsByCongressForCommittee(ID_CONGRESS, ID_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCallsByCongressForCommittee_WhenUserNotMember() {
        // Arrange
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_USER))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getCallsByCongressForCommittee(ID_CONGRESS, ID_USER));

        verify(callForPapersRepository, never()).findByCongress_IdCongress(any());
    }

    //----------------------- TESTS FOR GET SUBMISSIONS BY CALL FOR COMMITTEE -----------------------

    @Test
    void testGetSubmissionsByCallForCommittee() throws Exception {
        // Arrange
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_USER))
                .thenReturn(true);
        when(submissionRepository.findByCallForPapers_IdCall(ID_CALL))
                .thenReturn(List.of(pendingSubmission()));

        // Act
        List<SubmissionSummaryResponse> result =
                service.getSubmissionsByCallForCommittee(ID_CALL, ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(1,                result.size()),
                () -> assertEquals(ID_SUBMISSION,    result.get(0).getIdSubmission()),
                () -> assertEquals(SUBMISSION_TITLE, result.get(0).getTitle()),
                () -> assertEquals(STATUS_PENDING,   result.get(0).getSubmissionStatus()),
                () -> assertEquals(TYPE_PONENCIA,    result.get(0).getActivityType())
        );
    }

    @Test
    void testGetSubmissionsByCallForCommittee_ReturnsEmpty_WhenNoSubmissions() throws Exception {
        // Arrange
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_USER))
                .thenReturn(true);
        when(submissionRepository.findByCallForPapers_IdCall(ID_CALL)).thenReturn(List.of());

        // Act
        List<SubmissionSummaryResponse> result =
                service.getSubmissionsByCallForCommittee(ID_CALL, ID_USER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSubmissionsByCallForCommittee_WhenCallNotFound() {
        // Arrange
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getSubmissionsByCallForCommittee(ID_CALL, ID_USER));

        verify(scientificCommitteeRepository, never())
                .existsByCongress_IdCongressAndUser_IdUser(any(), any());
    }

    @Test
    void testGetSubmissionsByCallForCommittee_WhenUserNotMember() {
        // Arrange
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(openCall()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_USER))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getSubmissionsByCallForCommittee(ID_CALL, ID_USER));

        verify(submissionRepository, never()).findByCallForPapers_IdCall(any());
    }

    //----------------------- TESTS FOR GET SUBMISSION DETAILS FOR COMMITTEE -----------------------

    @Test
    void testGetSubmissionDetailsForCommittee() throws Exception {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(pendingSubmission()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_USER))
                .thenReturn(true);

        // Act
        SubmissionDetailsResponse result =
                service.getSubmissionDetailsForCommittee(ID_SUBMISSION, ID_USER);

        // Assert
        assertAll(
                () -> assertEquals(ID_SUBMISSION,    result.getIdSubmission()),
                () -> assertEquals(SUBMISSION_TITLE, result.getTitle()),
                () -> assertEquals(ABSTRACT_TEXT,    result.getAbstractText()),
                () -> assertEquals(FILE_URL,         result.getFileUrl()),
                () -> assertEquals(STATUS_PENDING,   result.getSubmissionStatus()),
                () -> assertEquals(TYPE_PONENCIA,    result.getActivityType()),
                () -> assertEquals(USER_NAME,        result.getAuthorName())
        );
    }

    @Test
    void testGetSubmissionDetailsForCommittee_WhenSubmissionNotFound() {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getSubmissionDetailsForCommittee(ID_SUBMISSION, ID_USER));

        verify(scientificCommitteeRepository, never())
                .existsByCongress_IdCongressAndUser_IdUser(any(), any());
    }

    @Test
    void testGetSubmissionDetailsForCommittee_WhenUserNotMember() {
        // Arrange
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(pendingSubmission()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_USER))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getSubmissionDetailsForCommittee(ID_SUBMISSION, ID_USER));
    }

    //----------------------- TESTS FOR EVALUATE SUBMISSION -----------------------

    @Test
    void testEvaluateSubmission_Approved() throws Exception {
        // Arrange
        EvaluationRequest request = new EvaluationRequest(COMMENTS, true);
        ArgumentCaptor<SubmissionEvaluationEntity> evalCaptor =
                ArgumentCaptor.forClass(SubmissionEvaluationEntity.class);
        ArgumentCaptor<SubmissionEntity> submissionCaptor =
                ArgumentCaptor.forClass(SubmissionEntity.class);

        SubmissionEvaluationEntity savedEval = evaluationEntity(true);

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(pendingSubmission()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_EVALUATOR))
                .thenReturn(true);
        when(userRepository.findById(ID_EVALUATOR)).thenReturn(Optional.of(evaluator()));
        when(submissionStatusRepository.findByStatusName(STATUS_APPROVED)).thenReturn(Optional.of(approvedStatus()));
        when(submissionEvaluationRepository.save(any())).thenReturn(savedEval);
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        EvaluationResponse result = service.evaluateSubmission(ID_SUBMISSION, ID_EVALUATOR, request);

        // Assert
        assertAll(
                () -> verify(submissionEvaluationRepository).save(evalCaptor.capture()),
                () -> assertEquals(COMMENTS,     evalCaptor.getValue().getComments()),
                () -> assertTrue(evalCaptor.getValue().getIsApproved()),
                () -> verify(submissionRepository).save(submissionCaptor.capture()),
                () -> assertEquals(STATUS_APPROVED, submissionCaptor.getValue().getSubmissionStatus().getStatusName()),
                () -> assertEquals(ID_EVALUATION, result.getIdEvaluation()),
                () -> assertTrue(result.getEvaluationStatus()),
                () -> assertEquals(COMMENTS,      result.getComments()),
                () -> verify(emailService).sendSubmissionEvaluationEmail(
                        eq(USER_EMAIL), eq(USER_NAME), eq(CONGRESS_NAME),
                        eq(SUBMISSION_TITLE), eq(TYPE_PONENCIA), eq(true), eq(COMMENTS))
        );
    }

    @Test
    void testEvaluateSubmission_Rejected() throws Exception {
        // Arrange
        EvaluationRequest request = new EvaluationRequest(COMMENTS, false);
        ArgumentCaptor<SubmissionEntity> submissionCaptor =
                ArgumentCaptor.forClass(SubmissionEntity.class);

        SubmissionEvaluationEntity savedEval = evaluationEntity(false);

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(pendingSubmission()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_EVALUATOR))
                .thenReturn(true);
        when(userRepository.findById(ID_EVALUATOR)).thenReturn(Optional.of(evaluator()));
        when(submissionStatusRepository.findByStatusName(STATUS_REJECTED)).thenReturn(Optional.of(rejectedStatus()));
        when(submissionEvaluationRepository.save(any())).thenReturn(savedEval);
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        EvaluationResponse result = service.evaluateSubmission(ID_SUBMISSION, ID_EVALUATOR, request);

        // Assert
        assertAll(
                () -> verify(submissionRepository).save(submissionCaptor.capture()),
                () -> assertEquals(STATUS_REJECTED, submissionCaptor.getValue().getSubmissionStatus().getStatusName()),
                () -> assertFalse(result.getEvaluationStatus()),
                () -> verify(emailService).sendSubmissionEvaluationEmail(
                        eq(USER_EMAIL), eq(USER_NAME), eq(CONGRESS_NAME),
                        eq(SUBMISSION_TITLE), eq(TYPE_PONENCIA), eq(false), eq(COMMENTS))
        );
    }

    @Test
    void testEvaluateSubmission_WhenSubmissionNotFound() {
        // Arrange
        EvaluationRequest request = new EvaluationRequest(COMMENTS, true);
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.evaluateSubmission(ID_SUBMISSION, ID_EVALUATOR, request));

        verifyNoInteractions(submissionEvaluationRepository, emailService);
    }

    @Test
    void testEvaluateSubmission_WhenEvaluatorNotMember() {
        // Arrange
        EvaluationRequest request = new EvaluationRequest(COMMENTS, true);
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(pendingSubmission()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_EVALUATOR))
                .thenReturn(false);

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.evaluateSubmission(ID_SUBMISSION, ID_EVALUATOR, request));

        verifyNoInteractions(submissionEvaluationRepository, emailService);
    }

    @Test
    void testEvaluateSubmission_WhenSubmissionIsNotPending() {
        // Arrange
        EvaluationRequest request   = new EvaluationRequest(COMMENTS, true);
        SubmissionEntity approved   = submissionWithStatus(approvedStatus());

        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(approved));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_EVALUATOR))
                .thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.evaluateSubmission(ID_SUBMISSION, ID_EVALUATOR, request));

        verifyNoInteractions(submissionEvaluationRepository, emailService);
    }

    @Test
    void testEvaluateSubmission_WhenEvaluatorNotFound() {
        // Arrange
        EvaluationRequest request = new EvaluationRequest(COMMENTS, true);
        when(submissionRepository.findById(ID_SUBMISSION)).thenReturn(Optional.of(pendingSubmission()));
        when(scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(ID_CONGRESS, ID_EVALUATOR))
                .thenReturn(true);
        when(userRepository.findById(ID_EVALUATOR)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.evaluateSubmission(ID_SUBMISSION, ID_EVALUATOR, request));

        verifyNoInteractions(submissionEvaluationRepository, emailService);
    }

    //----------------------- TESTS FOR GET MY EVALUATIONS -----------------------

    @Test
    void testGetMyEvaluations() {
        // Arrange
        SubmissionEvaluationEntity eval = evaluationEntity(true);
        when(submissionEvaluationRepository.findByEvaluator_IdUser(ID_EVALUATOR))
                .thenReturn(List.of(eval));

        // Act
        List<EvaluationDetailsResponse> result = service.getMyEvaluations(ID_EVALUATOR);

        // Assert
        assertAll(
                () -> assertEquals(1,            result.size()),
                () -> assertEquals(ID_EVALUATION, result.get(0).getIdEvaluation()),
                () -> assertTrue(result.get(0).getApproved()),
                () -> assertEquals(COMMENTS,      result.get(0).getComments())
        );
    }

    @Test
    void testGetMyEvaluations_ReturnsEmpty_WhenNoEvaluations() {
        // Arrange
        when(submissionEvaluationRepository.findByEvaluator_IdUser(ID_EVALUATOR))
                .thenReturn(List.of());

        // Act
        List<EvaluationDetailsResponse> result = service.getMyEvaluations(ID_EVALUATOR);

        // Assert
        assertTrue(result.isEmpty());
    }

    //----------------------- HELPER METHODS  -----------------------

    private CongressEntity congress() {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(ID_CONGRESS);
        congress.setCongressName(CONGRESS_NAME);
        congress.setStartDate(START_DATE);
        congress.setEndDate(END_DATE);
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
        user.setIdUser(ID_EVALUATOR);
        user.setFullName(EVALUATOR_NAME);
        user.setEmail("garcia@mail.com");
        user.setIsActive(true);
        return user;
    }

    private ScientificCommiteeEntity scientificCommitteeEntity() {
        ScientificCommiteeEntity sc = new ScientificCommiteeEntity();
        sc.setCongress(congress());
        sc.setUser(user());
        return sc;
    }

    private CallForPapersEntity openCall() {
        CallForPapersEntity call = new CallForPapersEntity();
        call.setIdCall(ID_CALL);
        call.setCallName(CALL_NAME);
        call.setDescription("Submit your papers");
        call.setIsOpen(true);
        call.setOpenDate(OPEN_DATE);
        call.setCloseDate(CLOSE_DATE);
        call.setCongress(congress());
        return call;
    }

    private ActivityTypeEntity activityType() {
        ActivityTypeEntity type = new ActivityTypeEntity();
        type.setIdActivityType(1);
        type.setTypeName(TYPE_PONENCIA);
        return type;
    }

    private SubmissionStatusEntity pendingStatus() {
        SubmissionStatusEntity status = new SubmissionStatusEntity();
        status.setIdStatus(1);
        status.setStatusName(STATUS_PENDING);
        return status;
    }

    private SubmissionStatusEntity approvedStatus() {
        SubmissionStatusEntity status = new SubmissionStatusEntity();
        status.setIdStatus(2);
        status.setStatusName(STATUS_APPROVED);
        return status;
    }

    private SubmissionStatusEntity rejectedStatus() {
        SubmissionStatusEntity status = new SubmissionStatusEntity();
        status.setIdStatus(3);
        status.setStatusName(STATUS_REJECTED);
        return status;
    }

    private SubmissionEntity pendingSubmission() {
        return submissionWithStatus(pendingStatus());
    }

    private SubmissionEntity submissionWithStatus(SubmissionStatusEntity status) {
        SubmissionEntity submission = new SubmissionEntity();
        submission.setIdSubmission(ID_SUBMISSION);
        submission.setUser(user());
        submission.setCallForPapers(openCall());
        submission.setActivityType(activityType());
        submission.setSubmissionStatus(status);
        submission.setSubmissionTitle(SUBMISSION_TITLE);
        submission.setAbstractText(ABSTRACT_TEXT);
        submission.setFileUrl(FILE_URL);
        submission.setSubmittedAt(LocalDateTime.now());
        return submission;
    }

    private SubmissionEvaluationEntity evaluationEntity(boolean approved) {
        SubmissionEvaluationEntity eval = new SubmissionEvaluationEntity();
        eval.setIdEvaluation(ID_EVALUATION);
        eval.setEvaluator(evaluator());
        eval.setSubmission(pendingSubmission());
        eval.setComments(COMMENTS);
        eval.setIsApproved(approved);
        return eval;
    }
}