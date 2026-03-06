package com.alessandro.congress_management.repositories.activitypresenter;

import com.alessandro.congress_management.dto.activitypresenter.ActivityPresenterResponse;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityPresenterRepository extends JpaRepository<ActivityPresenterEntity, Long> {

    boolean existsByActivity_IdActivityAndUser_IdUser(Long idActivity, Long idUser);

    int countByActivity_IdActivity(Long idActivity);

    List<ActivityPresenterEntity> findByActivity_IdActivity(Long idActivity);

    boolean existsByActivity_IdActivityAndUser_IdUserAndIsInvitedSpeakerTrue(Long idActivity, Long idUser);

    ActivityPresenterEntity findByActivity_IdActivityAndUser_IdUser(Long idActivity, Long idUser);
}
