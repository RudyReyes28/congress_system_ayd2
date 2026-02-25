package com.alessandro.congress_management.repositories.congressadministrator;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressAdministratorEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface CongressAdministratorRepository extends JpaRepository<CongressAdministratorEntity, Long> {
    boolean existsByUserAndCongress(UserEntity user, CongressEntity congress);

    List<CongressAdministratorEntity> findByUser_IdUser(Long idUser);

    List<CongressAdministratorEntity> findByCongress_IdCongress(Long idCongress);

    Optional<CongressAdministratorEntity> findByUser_IdUserAndCongress_IdCongress(Long idUser, Long idCongress);

    int countByCongress_IdCongress(Long idCongress);
}
