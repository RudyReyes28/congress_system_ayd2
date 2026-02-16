package com.alessandro.congress_management.repositories.authenticate;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByIdentificationNumber(String identificationNumber);

    boolean existsByUsername(String username);

    boolean existsByIdentificationNumber(String identificationNumber);

    Optional <UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
