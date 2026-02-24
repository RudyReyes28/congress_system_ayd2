package com.alessandro.congress_management.repositories.congressadministrator;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressAdministratorEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CongressAdministratorRepository extends JpaRepository<CongressAdministratorEntity, Long> {
    boolean existsByUserAndCongress(UserEntity user, CongressEntity congress);

    List<CongressAdministratorEntity> findByUser_IdUser(Long idUser);
}
