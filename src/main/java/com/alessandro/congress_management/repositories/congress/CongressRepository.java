package com.alessandro.congress_management.repositories.congress;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CongressRepository extends JpaRepository<CongressEntity, Long> {
    boolean existsByIsActiveTrueAndInstitution_IdInstitution(Long idInstitution);

    boolean existsByCongressName(String congressName);

    boolean existsByCongressNameAndIdCongressNot(String congressName, Long idCongress);

    List<CongressEntity> findByIsActiveTrue();


    List<CongressEntity> findByStartDateBetweenAndInstitution_IdInstitution(LocalDate start, LocalDate end, Long idInstitution);

    List<CongressEntity> findByStartDateBetween(LocalDate start, LocalDate end);

    List<CongressEntity> findByInstitution_IdInstitution(Long idInstitution);
}
