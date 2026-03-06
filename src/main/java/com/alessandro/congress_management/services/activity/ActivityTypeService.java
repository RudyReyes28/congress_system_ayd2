package com.alessandro.congress_management.services.activity;

import com.alessandro.congress_management.dto.activity.ActivityTypeResponse;

import java.util.List;

public interface ActivityTypeService {
    List<ActivityTypeResponse> getAllActivityTypes();
}
