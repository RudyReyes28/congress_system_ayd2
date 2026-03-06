package com.alessandro.congress_management.services.activity;

import com.alessandro.congress_management.dto.activity.ActivityTypeResponse;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityTypeServiceImpl implements ActivityTypeService {
    private final ActivityTypeRepository activityTypeRepository;

    public ActivityTypeServiceImpl(ActivityTypeRepository activityTypeRepository) {
        this.activityTypeRepository = activityTypeRepository;
    }

    @Override
    public List<ActivityTypeResponse> getAllActivityTypes() {
        List<ActivityTypeEntity> activityTypes = activityTypeRepository.findAll();
        return activityTypes.stream()
                .map(ActivityTypeResponse::fromEntity)
                .toList();
    }
}
