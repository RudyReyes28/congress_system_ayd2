package com.alessandro.congress_management.dto.submissions.submission;

import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class SubmissionResponse {
    Long idSubmission;
    Long idCall;
    Long idUser;
    String authorName;
    Integer idActivityType;
    String activityTypeName;
    Integer idStatus;
    String statusName;
    String submissionTitle;
    String abstrakt;
    String fileUrl;
    LocalDateTime submittedAt;
    LocalDateTime updatedAt;

     public static SubmissionResponse fromEntity(SubmissionEntity submission) {
        return new SubmissionResponse(
                submission.getIdSubmission(),
                submission.getCallForPapers().getIdCall(),
                submission.getUser().getIdUser(),
                submission.getUser().getFullName(),
                submission.getActivityType().getIdActivityType(),
                submission.getActivityType().getTypeName(),
                submission.getSubmissionStatus().getIdStatus(),
                submission.getSubmissionStatus().getStatusName(),
                submission.getSubmissionTitle(),
                submission.getAbstractText(),
                submission.getFileUrl(),
                submission.getSubmittedAt(),
                submission.getUpdatedAt()
        );
    }

}
