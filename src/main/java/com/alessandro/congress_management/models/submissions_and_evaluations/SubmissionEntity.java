package com.alessandro.congress_management.models.submissions_and_evaluations;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "Submission")
@Table(name = "submission")
@Data
@NoArgsConstructor
public class SubmissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_submission")
    private Long idSubmission;

    @ManyToOne
    @JoinColumn(name = "id_call", referencedColumnName = "id_call")
    private CallForPapersEntity callForPapers;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "id_activity_type", referencedColumnName = "id_activity_type")
    private ActivityTypeEntity activityType;

    @ManyToOne
    @JoinColumn(name = "id_status", referencedColumnName = "id_status")
    private SubmissionStatusEntity submissionStatus;

    @Column(name = "submission_title")
    private String submissionTitle;

    @Column(name = "abstract", columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "submitted_at")
    private Timestamp submittedAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
