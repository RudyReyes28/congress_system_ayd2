package com.alessandro.congress_management.repositories.institution;

import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<InstitutionEntity, Long> {

    Optional<List<InstitutionEntity>> findByIsActiveTrue();

    boolean existsByInstitutionName(String institutionName);

    boolean existsByContactEmail(String contactEmail);

    boolean existsByInstitutionNameAndIdInstitutionNot(String institutionName, Long idInstitution);
}
