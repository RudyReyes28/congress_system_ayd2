package com.alessandro.congress_management.dto.submissions.evaluation;

import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEvaluationEntity;
import lombok.Value;

@Value
public class EvaluationDetailsResponse {
    Long idEvaluation;
    Boolean approved;
    String comments;
    SubmissionDetailsResponse submissionDetails;

    public static EvaluationDetailsResponse fromEntity(SubmissionEvaluationEntity evaluation) {
        return new EvaluationDetailsResponse(
                evaluation.getIdEvaluation(),
                evaluation.getIsApproved(),
                evaluation.getComments(),
                SubmissionDetailsResponse.fromEntity(evaluation.getSubmission())
        );
    }
}
