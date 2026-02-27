package com.alessandro.congress_management.repositories.activity;

import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {
    boolean existsByRoom_IdRoom(Long idRoom);
}
