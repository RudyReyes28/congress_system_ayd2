package com.alessandro.congress_management.dto.submissions.submission;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionStatusEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class SubmissionRequest {

    @NotNull(message = "Activity type is required")
    Integer idActivityType;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String submissionTitle;

    @NotBlank(message = "Abstract is required")
    String abstrakt;

    public SubmissionEntity toEntity(CallForPapersEntity call, UserEntity user, ActivityTypeEntity type, SubmissionStatusEntity pending, String fileUrl) {
        SubmissionEntity submission = new SubmissionEntity();
        submission.setCallForPapers(call);
        submission.setUser(user);
        submission.setActivityType(type);
        submission.setSubmissionStatus(pending);
        submission.setSubmissionTitle(submissionTitle);
        submission.setAbstractText(abstrakt);
        submission.setFileUrl(fileUrl);
        return submission;
    }
}
