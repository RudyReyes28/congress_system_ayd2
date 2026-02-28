package com.alessandro.congress_management.services.activity;


import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.activity.CreateActivityRequest;
import com.alessandro.congress_management.dto.activity.UpdateActivityRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;

import java.util.List;

public interface ActivityService {
    ActivityResponse createActivity(Long congressId, CreateActivityRequest request) throws NotFoundException, BusinessRuleException;

    ActivityResponse updateActivity(Long activityId, UpdateActivityRequest request) throws NotFoundException, BusinessRuleException;

    void deleteActivity(Long activityId) throws NotFoundException, BusinessRuleException;

    ActivityEntity getActivityById(Long activityId) throws NotFoundException;

    List<ActivityResponse> getActivitiesByRoomId(Long roomId) throws NotFoundException;

    List<ActivityResponse> getActivitiesByCongressId(Long congressId) throws NotFoundException;
}
