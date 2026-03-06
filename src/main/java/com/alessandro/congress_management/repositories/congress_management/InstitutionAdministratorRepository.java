package com.alessandro.congress_management.repositories.congress_management;

import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionAdministratorRepository extends JpaRepository<InstitutionAdministratorEntity, Long> {
    Optional<InstitutionAdministratorEntity> findByUser_IdUser(Long idUser);

    boolean existsByUser_IdUserAndInstitution_IdInstitution(Long idUser, Long idInstitution);

    List<InstitutionAdministratorEntity> findByInstitution_IdInstitution(Long idInstitution);

    List<InstitutionAdministratorEntity> findByInstitution_InstitutionName(String institutionName);
}
