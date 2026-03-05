package com.alessandro.congress_management.dto.submissions.evaluation;

import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class SubmissionDetailsResponse {
    Long idSubmission;
    String authorName;
    String title;
    String activityType;
    String submissionStatus;
    String abstractText;
    String fileUrl;
    LocalDateTime submittedAt;

    public static SubmissionDetailsResponse fromEntity(SubmissionEntity submission) {
        return new SubmissionDetailsResponse(
                submission.getIdSubmission(),
                submission.getUser().getFullName(),
                submission.getSubmissionTitle(),
                submission.getActivityType().getTypeName(),
                submission.getSubmissionStatus().getStatusName(),
                submission.getAbstractText(),
                submission.getFileUrl(),
                submission.getSubmittedAt()
        );
    }

}
