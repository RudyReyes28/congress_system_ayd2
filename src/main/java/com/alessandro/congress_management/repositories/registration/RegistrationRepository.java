package com.alessandro.congress_management.repositories.registration;

import com.alessandro.congress_management.models.registrations_and_payments.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {
    boolean existsByCongress_IdCongress(Long idCongress);
}
