package com.alessandro.congress_management.dto.activity;

import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import lombok.Value;

@Value
public class ActivityResponse {
    Long idActivity;
    String activityName;
    String description;
    String activityType;
    String startTime;
    String endTime;
    String roomName;
    Integer maxCapacity;

    public static ActivityResponse fromEntity(ActivityEntity activity) {
        return new ActivityResponse(
                activity.getIdActivity(),
                activity.getActivityName(),
                activity.getDescription(),
                activity.getActivityType().getTypeName(),
                activity.getStartTime().toString(),
                activity.getEndTime().toString(),
                activity.getRoom().getRoomName(),
                activity.getMaxCapacity()
        );
    }

}
