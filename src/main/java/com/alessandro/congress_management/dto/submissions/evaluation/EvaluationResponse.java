package com.alessandro.congress_management.dto.submissions.evaluation;

import lombok.Value;

@Value
public class EvaluationResponse {
    Long idEvaluation;
    Boolean evaluationStatus;
    String comments;

    public static EvaluationResponse fromEntity(Long idEvaluation, Boolean evaluationStatus, String comments) {
        return new EvaluationResponse(
                idEvaluation,
                evaluationStatus,
                comments
        );
    }
}
