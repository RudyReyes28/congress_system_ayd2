package com.alessandro.congress_management.services.submissions.evaluation;

import com.alessandro.congress_management.dto.submissions.evaluation.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
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
import com.alessandro.congress_management.services.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluationScientificCommiteeServiceImpl  implements EvaluationScientificCommiteeService{
    private static final String STATUS_PENDING  = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_ACCEPTED = "APPROVED";

    private final SubmissionRepository submissionRepository;
    private final CallForPapersRepository callForPapersRepository;
    private final UserRepository userRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final SubmissionStatusRepository submissionStatusRepository;
    private final SubmissionEvaluationRepository submissionEvaluationRepository;
    private final CongressRepository congressRepository;
    private final ScientificCommiteeRepository scientificCommitteeRepository;
    private final EmailService emailService;

    public EvaluationScientificCommiteeServiceImpl(SubmissionRepository submissionRepository, CallForPapersRepository callForPapersRepository, UserRepository userRepository, ActivityTypeRepository activityTypeRepository, SubmissionStatusRepository submissionStatusRepository, SubmissionEvaluationRepository submissionEvaluationRepository, CongressRepository congressRepository, ScientificCommiteeRepository scientificCommitteeRepository, EmailService emailService) {
        this.submissionRepository = submissionRepository;
        this.callForPapersRepository = callForPapersRepository;
        this.userRepository = userRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.submissionStatusRepository = submissionStatusRepository;
        this.submissionEvaluationRepository = submissionEvaluationRepository;
        this.congressRepository = congressRepository;
        this.scientificCommitteeRepository = scientificCommitteeRepository;
        this.emailService = emailService;
    }


    @Override
    public List<CongressSummaryResponse> getMyCommitteeCongresses(Long userId) throws NotFoundException {
        //Obtenemos el usuario para validar su existencia
        UserEntity user = findUser(userId);
        //Obtenemos los congresos donde el usuario es parte del comité científico
        List<ScientificCommiteeEntity> committees = findScientificCommitteeByUser(userId);
        //Mapeamos a DTO de respuesta
        return committees.stream()
                .map(sc -> new CongressSummaryResponse(
                        sc.getCongress().getIdCongress(),
                        sc.getCongress().getCongressName(),
                        sc.getCongress().getStartDate(),
                        sc.getCongress().getEndDate()
                ))
                .toList();
    }

    @Override
    public List<CallSummaryResponse> getCallsByCongressForCommittee(Long congressId, Long userId) throws NotFoundException {
        validateMemberCongressAndUser(userId, congressId);
        List<CallForPapersEntity> calls = callForPapersRepository.findByCongress_IdCongress(congressId);
        return calls.stream()
                .map(CallSummaryResponse::fromEntity)
                .toList();
    }

    @Override
    public List<SubmissionSummaryResponse> getSubmissionsByCallForCommittee(Long callId, Long userId) throws NotFoundException, BusinessRuleException {
        //Obtenemos la convocatoria para validar su existencia
        CallForPapersEntity call = findOpenCall(callId);
        //Validamos que el usuario sea parte del comité científico del congreso asociado a la convocatoria
        validateMemberCongressAndUser(userId, call.getCongress().getIdCongress());
        //Obtenemos las submissions asociadas a la convocatoria
        List<SubmissionEntity> submissions = submissionRepository.findByCallForPapers_IdCall(callId);
        //Mapeamos a DTO de respuesta
        return submissions.stream()
                .map(SubmissionSummaryResponse::fromEntity)
                .toList();
    }


    @Override
    public SubmissionDetailsResponse getSubmissionDetailsForCommittee(Long submissionId, Long userId) throws NotFoundException {
        //Obtenemos la submission para validar su existencia
        SubmissionEntity submission = findSubmission(submissionId);
        //Validamos que el usuario sea parte del comité científico del congreso asociado a la convocatoria de la submission
        validateMemberCongressAndUser(userId, submission.getCallForPapers().getCongress().getIdCongress());
        //Mapeamos a DTO de respuesta
        return SubmissionDetailsResponse.fromEntity(submission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationResponse evaluateSubmission(Long submissionId, Long evaluatorId, EvaluationRequest request) throws NotFoundException, BusinessRuleException {
        //Obtenemos la submission para validar su existencia
        SubmissionEntity submission = findSubmission(submissionId);
        //Validamos que el evaluador sea parte del comite científico del congreso asociado a la convocatoria de la submission
        validateMemberCongressAndUser(evaluatorId, submission.getCallForPapers().getCongress().getIdCongress());
        //Validamos que la submission este en estado PENDING
        validateSubmissionStatePending(submission);
        //Creamos la evaluacion
        SubmissionEvaluationEntity evaluation =  submissionEvaluationRepository.save(request.toEntity(findUser(evaluatorId), submission));
        //Actualizamos el estado de la submission
        if(request.getApproved()){
            submission.setSubmissionStatus(findStatus(STATUS_ACCEPTED));
        } else {
            submission.setSubmissionStatus(findStatus(STATUS_REJECTED));
        }
        submissionRepository.save(submission);
        //Enviamos un email al autor de la submission con el resultado de la evaluacion
        emailService.sendSubmissionEvaluationEmail(
                submission.getUser().getEmail(),
                submission.getUser().getFullName(),
                submission.getCallForPapers().getCongress().getCongressName(),
                submission.getSubmissionTitle(),
                submission.getActivityType().getTypeName(),
                request.getApproved(),
                request.getComments()
        );
        //Mapeamos a DTO de respuesta
        return EvaluationResponse.fromEntity(
                evaluation.getIdEvaluation(),
                evaluation.getIsApproved(),
                evaluation.getComments()
        );
    }

    @Override
    public List<EvaluationDetailsResponse> getMyEvaluations(Long userId) {
        List<SubmissionEvaluationEntity> evaluations = submissionEvaluationRepository.findByEvaluator_IdUser(userId);
        return evaluations.stream()
                .map(EvaluationDetailsResponse::fromEntity)
                .toList();


    }


    //Metodos auxiliares para validaciones y conversiones

    private CallForPapersEntity findOpenCall(Long idCall) throws NotFoundException, BusinessRuleException {
        CallForPapersEntity call = callForPapersRepository.findById(idCall)
                .orElseThrow(() -> new NotFoundException("Call for papers not found"));
        return call;
    }

    private UserEntity findUser(Long idUser) throws NotFoundException {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }



    private SubmissionStatusEntity findStatus(String statusName) throws NotFoundException {
        return submissionStatusRepository.findByStatusName(statusName)
                .orElseThrow(() -> new NotFoundException("Submission status not found"));
    }

    private SubmissionEntity findSubmission(Long idSubmission) throws NotFoundException {
        return submissionRepository.findById(idSubmission)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
    }

    private List<ScientificCommiteeEntity> findScientificCommitteeByUser(Long userId) throws NotFoundException {
        return scientificCommitteeRepository.findByUser_IdUser(userId);
    }

    private void validateMemberCongressAndUser(Long userId, Long congressId) throws NotFoundException {
        if (!scientificCommitteeRepository.existsByCongress_IdCongressAndUser_IdUser(congressId, userId)) {
            throw new NotFoundException("User is not a member of the scientific committee for this congress");
        }
    }

    private void validateSubmissionStatePending(SubmissionEntity submission) throws BusinessRuleException {
        if (!submission.getSubmissionStatus().getStatusName().equals(STATUS_PENDING)) {
            throw new BusinessRuleException("Only submissions with PENDING status can be evaluated");
        }
    }

}
