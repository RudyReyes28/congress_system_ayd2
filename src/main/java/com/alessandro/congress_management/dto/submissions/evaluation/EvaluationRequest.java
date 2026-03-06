package com.alessandro.congress_management.dto.submissions.evaluation;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEvaluationEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class EvaluationRequest {
    @NotBlank(message = "Comments cannot be blank")
    String comments;
    @NotNull(message = "Approval status must be provided")
    Boolean approved;

    public SubmissionEvaluationEntity toEntity(UserEntity evaluator, SubmissionEntity submission) {
        SubmissionEvaluationEntity evaluation = new SubmissionEvaluationEntity();
        evaluation.setEvaluator(evaluator);
        evaluation.setSubmission(submission);
        evaluation.setComments(this.comments);
        evaluation.setIsApproved(this.approved);
        return evaluation;
    }

}
