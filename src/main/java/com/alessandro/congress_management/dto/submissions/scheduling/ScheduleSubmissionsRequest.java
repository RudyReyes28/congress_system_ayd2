package com.alessandro.congress_management.dto.submissions.scheduling;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ScheduleSubmissionsRequest {
    @NotNull(message = "Room ID is required")
    Long roomId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    LocalDateTime startTime;

    @NotNull(message = "End time is required")
    LocalDateTime endTime;
    Integer maxCapacity;

    public ActivityEntity toEntity(CongressEntity congress, RoomEntity room, ActivityTypeEntity activityType, String activityName, String description) {
        ActivityEntity activity = new ActivityEntity();
        activity.setCongress(congress);
        activity.setRoom(room);
        activity.setActivityType(activityType);
        activity.setActivityName(activityName);
        activity.setDescription(description);
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setMaxCapacity(maxCapacity);
        return activity;

    }
}
