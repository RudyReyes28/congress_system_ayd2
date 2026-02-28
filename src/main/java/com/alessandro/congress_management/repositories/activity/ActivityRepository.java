package com.alessandro.congress_management.repositories.activity;

import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {
    boolean existsByRoom_IdRoom(Long idRoom);

    boolean existsByRoom_IdRoomAndStartTimeLessThanAndEndTimeGreaterThan(Long room_idRoom, LocalDateTime startTime, LocalDateTime endTime);

    boolean existsByRoom_IdRoomAndStartTimeLessThanAndEndTimeGreaterThanAndIdActivityNot(Long room_idRoom, LocalDateTime startTime, LocalDateTime endTime, Long idActivity);

    List<ActivityEntity> findByRoom_IdRoom(Long roomId);

    List<ActivityEntity> findByCongress_IdCongress(Long congressId);
}
