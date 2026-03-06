package com.alessandro.congress_management.dto.activity;

import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import lombok.Value;

@Value
public class ActivityTypeResponse {
    Integer idActivityType;
    String activityTypeName;

    public static ActivityTypeResponse fromEntity(ActivityTypeEntity activityTypeEntity) {
        return new ActivityTypeResponse(
                activityTypeEntity.getIdActivityType(),
                activityTypeEntity.getTypeName()
        );
    }
}
