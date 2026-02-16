package com.alessandro.congress_management.repositories.authenticate;

import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    boolean existsByRoleName(String roleName);

    Optional<RoleEntity> findByRoleName(String roleName);
}
