package com.alessandro.congress_management.repositories.attendance;

import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    boolean existsByActivity_IdActivity(Long activityId);
}
