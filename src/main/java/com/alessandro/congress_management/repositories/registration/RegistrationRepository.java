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

    List<RegistrationEntity> findByUser_IdUser(Long userId);

    List<RegistrationEntity> findByCongress_IdCongress(Long congressId);

    int countByCongress_IdCongress(Long congressId);

    int countByUser_IdUserAndCongress_IdCongress(Long userId, Long congressId);


}
