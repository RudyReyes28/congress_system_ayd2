package com.alessandro.congress_management.repositories.congress_management;

import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionAdministratorRepository extends JpaRepository<InstitutionAdministratorEntity, Long> {

}
