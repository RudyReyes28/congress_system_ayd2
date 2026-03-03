package com.alessandro.congress_management.repositories.submissions.submission;

import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {
    boolean existsByCallForPapers_IdCall(Long callId);

    List<SubmissionEntity> findByCallForPapers_IdCall(Long idCall);

    List<SubmissionEntity> findByUser_IdUser(Long idUser);
}
