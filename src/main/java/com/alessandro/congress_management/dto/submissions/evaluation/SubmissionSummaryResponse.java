package com.alessandro.congress_management.dto.submissions.evaluation;

import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import lombok.Value;

@Value
public class SubmissionSummaryResponse {
    Long idSubmission;
    String authorName;
    String title;
    String activityType;
    String submissionStatus;

    public static SubmissionSummaryResponse fromEntity(SubmissionEntity submission) {
        return new SubmissionSummaryResponse(
                submission.getIdSubmission(),
                submission.getUser().getFullName(),
                submission.getSubmissionTitle(),
                submission.getActivityType().getTypeName(),
                submission.getSubmissionStatus().getStatusName()
        );
    }

}
