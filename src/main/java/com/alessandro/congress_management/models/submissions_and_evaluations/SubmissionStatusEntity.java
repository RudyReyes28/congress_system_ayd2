package com.alessandro.congress_management.models.submissions_and_evaluations;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "SubmissionStatus")
@Table(name = "submission_status")
@Data
@NoArgsConstructor
public class SubmissionStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status")
    private Long idStatus;

    @Column(name = "status_name", nullable = false, unique = true)
    private String statusName;
}
