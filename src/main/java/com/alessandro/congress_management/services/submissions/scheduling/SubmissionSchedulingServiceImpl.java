package com.alessandro.congress_management.services.submissions.scheduling;

import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.activity.CreateActivityRequest;
import com.alessandro.congress_management.dto.activitypresenter.ActivityPresenterRequest;
import com.alessandro.congress_management.dto.submissions.evaluation.EvaluationDetailsResponse;
import com.alessandro.congress_management.dto.submissions.evaluation.SubmissionSummaryResponse;
import com.alessandro.congress_management.dto.submissions.scheduling.EvaluationSubmissionDetails;
import com.alessandro.congress_management.dto.submissions.scheduling.ScheduleSubmissionsRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEvaluationEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionStatusEntity;
import com.alessandro.congress_management.repositories.submissions.evaluation.SubmissionEvaluationRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionStatusRepository;
import com.alessandro.congress_management.services.activity.ActivityService;
import com.alessandro.congress_management.services.activitypresenter.ActivityPresenterService;
import com.alessandro.congress_management.services.email.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubmissionSchedulingServiceImpl implements SubmissionSchedulingService{
    private static final String STATUS_PENDING  = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_ACCEPTED = "APPROVED";
    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final SubmissionRepository submissionRepository;
    private final SubmissionStatusRepository submissionStatusRepository;
    private final SubmissionEvaluationRepository submissionEvaluationRepository;
    private final EmailService emailService;
    private final ActivityService activityService;
    private final ActivityPresenterService activityPresenterService;

    public SubmissionSchedulingServiceImpl(SubmissionRepository submissionRepository, SubmissionStatusRepository submissionStatusRepository, SubmissionEvaluationRepository submissionEvaluationRepository, EmailService emailService, ActivityService activityService, ActivityPresenterService activityPresenterService) {
        this.submissionRepository = submissionRepository;
        this.submissionStatusRepository = submissionStatusRepository;
        this.submissionEvaluationRepository = submissionEvaluationRepository;
        this.emailService = emailService;
        this.activityService = activityService;
        this.activityPresenterService = activityPresenterService;
    }


    @Override
    public List<EvaluationSubmissionDetails> getEvaluationSubmissionsByIdCall(Long idCall) throws NotFoundException {
        List<SubmissionEvaluationEntity> evaluations = submissionEvaluationRepository.findBySubmission_CallForPapers_IdCall(idCall);
        return evaluations.stream()
                .map(EvaluationSubmissionDetails::fromEntity)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityResponse scheduleSubmissions(Long idSubmission, ScheduleSubmissionsRequest request) throws NotFoundException, BusinessRuleException {
        SubmissionEntity submission = getSubmissionById(idSubmission);
        validateSubmissionIsApproved(submission);

        //Creamos un activity request
        CreateActivityRequest activityRequest = new CreateActivityRequest(
                request.getRoomId(),
                submission.getActivityType().getIdActivityType(),
                submission.getSubmissionTitle(),
                submission.getAbstractText(),
                request.getStartTime(),
                request.getEndTime(),
                request.getMaxCapacity()
        );
        //Creamos la actividad asociada a la submission
        ActivityResponse activityResponse = activityService.createActivity(submission.getCallForPapers().getCongress().getIdCongress(), activityRequest);

        //Asignamos el presentador principal a la actividad
        ActivityPresenterRequest presenterRequest = new ActivityPresenterRequest(submission.getUser().getIdUser(), false);
        activityPresenterService.assignExistingUser(presenterRequest, activityResponse.getIdActivity());

        // Actualizamos el estado de la submission a SCHEDULED
        submission.setSubmissionStatus(getScheduledStatus(STATUS_SCHEDULED));
        submissionRepository.save(submission);

        // Enviamos un correo al autor notificandole que su submission ha sido programada
        emailService.sendPresenterAcceptedEmail(submission.getUser().getEmail(),
                submission.getUser().getFullName(),
                submission.getCallForPapers().getCongress().getCongressName(),
                activityResponse.getActivityName(),
                activityResponse.getActivityType(), false);

        // Retornamos la actividad creada
        return activityResponse;

    }

    @Override
    public void cancelScheduling(Long idSubmission) throws NotFoundException {
        //Obtenemos la submission
        SubmissionEntity submission = getSubmissionById(idSubmission);
        //Validamos que la submission este aprobada
        validateSubmissionIsApproved(submission);
        //Actualizamos el estado de la submission a CANCELLED
        submission.setSubmissionStatus(getScheduledStatus(STATUS_CANCELLED));
        submissionRepository.save(submission);
         // Enviamos un correo al autor notificandole que su submission ha sido cancelada
        emailService.sendSubmissionCancelledEmail(submission.getUser().getEmail(),
                submission.getUser().getFullName(),
                submission.getCallForPapers().getCongress().getCongressName(),
                submission.getSubmissionTitle(), submission.getActivityType().getTypeName());

    }

    private void validateSubmissionIsApproved(SubmissionEntity submission) throws NotFoundException {
        if (!submission.getSubmissionStatus().getStatusName().equals(STATUS_ACCEPTED)) {
            throw new NotFoundException("Submission with is not approved.");
        }
    }

    private SubmissionEntity getSubmissionById(Long idSubmission) throws NotFoundException {
        return submissionRepository.findById(idSubmission)
                .orElseThrow(() -> new NotFoundException("Submission not found."));
    }

    private SubmissionStatusEntity getScheduledStatus(String status) throws NotFoundException {
        return submissionStatusRepository.findByStatusName(status)
                .orElseThrow(() -> new NotFoundException("Scheduled status not found."));
    }
}
