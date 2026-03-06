package com.alessandro.congress_management.repositories.attendance;

import com.alessandro.congress_management.models.attendance.ParticipationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ParticipationTypeRepository extends JpaRepository<ParticipationTypeEntity, Integer> {
    Optional<ParticipationTypeEntity> findByTypeName(String name);
}
