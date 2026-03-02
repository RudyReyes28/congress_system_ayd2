package com.alessandro.congress_management.repositories.registration;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.registrations_and_payments.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {
    boolean existsByCongress_IdCongress(Long idCongress);

    @Query("""
    SELECT r.user
    FROM Registration r
    WHERE r.congress.idCongress = :congressId
      AND r.user.isActive = true
""")
    List<UserEntity> findActiveUsersByCongressId(@Param("congressId") Long congressId);

    boolean existsByUser_IdUserAndCongress_IdCongress(Long idUser, Long idCongress);
}
