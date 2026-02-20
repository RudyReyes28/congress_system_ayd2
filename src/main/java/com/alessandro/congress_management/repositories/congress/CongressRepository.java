package com.alessandro.congress_management.repositories.congress;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CongressRepository extends JpaRepository<CongressEntity, Long> {
    boolean existsByIsActiveTrueAndInstitution_IdInstitution(Long idInstitution);
}
