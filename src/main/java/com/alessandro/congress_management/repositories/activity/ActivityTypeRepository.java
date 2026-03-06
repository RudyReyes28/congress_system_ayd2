package com.alessandro.congress_management.repositories.activity;

import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityTypeRepository extends JpaRepository<ActivityTypeEntity, Integer> {
    boolean existsByTypeNameAndIdActivityType(String typeName, Integer idActivityType);
}
