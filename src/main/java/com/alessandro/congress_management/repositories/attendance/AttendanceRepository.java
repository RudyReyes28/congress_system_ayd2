package com.alessandro.congress_management.repositories.attendance;

import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    boolean existsByActivity_IdActivity(Long activityId);

    boolean existsByUser_IdUserAndActivity_IdActivity(Long idUser, Long idActivity);

    List<AttendanceEntity> findByUser_IdUser(Long idUser);

    List<AttendanceEntity> findByActivity_IdActivity(Long idActivity);

    List<AttendanceEntity> findByActivity_Congress_IdCongress(Long idCongress);
}
