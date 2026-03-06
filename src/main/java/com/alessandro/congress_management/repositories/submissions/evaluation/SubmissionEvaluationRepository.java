package com.alessandro.congress_management.repositories.submissions.evaluation;

import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionEvaluationRepository extends JpaRepository<SubmissionEvaluationEntity, Long> {
    List<SubmissionEvaluationEntity> findByEvaluator_IdUser(Long userId);

    List<SubmissionEvaluationEntity> findBySubmission_CallForPapers_IdCall(Long idCall);
}
