package com.alessandro.congress_management.repositories.authenticate;

import com.alessandro.congress_management.models.authentication_and_users.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    <Optional> PersonEntity findByEmail(String email);
    boolean existsByEmail(String email);
}
