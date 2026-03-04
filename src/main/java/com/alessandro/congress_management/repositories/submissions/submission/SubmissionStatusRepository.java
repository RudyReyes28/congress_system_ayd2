package com.alessandro.congress_management.repositories.submissions.submission;

import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubmissionStatusRepository extends JpaRepository<SubmissionStatusEntity, Integer> {
    Optional<SubmissionStatusEntity> findByStatusName(String idStatus);
}
