package com.alessandro.congress_management.models.submissions_and_evaluations;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "SubmissionEvaluation")
@Table(name = "submission_evaluation")
@Data
@NoArgsConstructor
public class SubmissionEvaluationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluation")
    private Long idEvaluation;

    @ManyToOne
    @JoinColumn(name = "id_submission", referencedColumnName = "id_submission")
    private SubmissionEntity submission;

    @ManyToOne
    @JoinColumn(name = "id_evaluator", referencedColumnName = "id_user")
    private UserEntity evaluator;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "evaluated_at")
    private Timestamp evaluatedAt;
}
