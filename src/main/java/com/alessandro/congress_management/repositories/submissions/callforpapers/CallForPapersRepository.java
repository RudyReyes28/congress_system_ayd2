package com.alessandro.congress_management.repositories.submissions.callforpapers;

import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface CallForPapersRepository extends JpaRepository<CallForPapersEntity, Long> {
    boolean existsByCongress_IdCongressAndIsOpenTrue(Long congressId);

    List<CallForPapersEntity> findByCongress_IdCongress(Long idCongress);

    Optional<CallForPapersEntity> findByCongress_IdCongressAndIsOpenTrue(Long congressId);
}
