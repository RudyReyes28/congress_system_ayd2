package com.alessandro.congress_management.dto.submissions.scheduling;

import com.alessandro.congress_management.dto.submissions.evaluation.EvaluationDetailsResponse;
import com.alessandro.congress_management.dto.submissions.evaluation.SubmissionDetailsResponse;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEvaluationEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class EvaluationSubmissionDetails {
    Long idEvaluation;
    String nameEvaluator;
    Boolean approved;
    String comments;
    LocalDateTime evaluatedAt;
    SubmissionDetailsResponse submissionDetails;

    public static EvaluationSubmissionDetails fromEntity(SubmissionEvaluationEntity evaluation) {
        return new EvaluationSubmissionDetails(
                evaluation.getIdEvaluation(),
                evaluation.getEvaluator().getFullName(),
                evaluation.getIsApproved(),
                evaluation.getComments(),
                evaluation.getEvaluatedAt(),
                SubmissionDetailsResponse.fromEntity(evaluation.getSubmission())
        );
    }
}
