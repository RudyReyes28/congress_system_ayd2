package com.alessandro.congress_management.repositories.workshopreservation;

import com.alessandro.congress_management.models.workshop_reservation.WorkshopReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkshopReservationRepository extends JpaRepository<WorkshopReservationEntity, Long> {
    int countByActivity_IdActivity(Long activityId);

    boolean existsByActivity_IdActivity(Long activityId);

    boolean existsByUser_IdUserAndActivity_IdActivity(Long userIdUser, Long activityIdActivity);

    List<WorkshopReservationEntity> findByUser_IdUser(Long idUser);

    List<WorkshopReservationEntity> findByActivity_IdActivity(Long activityIdActivity);
}
