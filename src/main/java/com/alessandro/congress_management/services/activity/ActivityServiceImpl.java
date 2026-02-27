package com.alessandro.congress_management.services.activity;

import org.springframework.stereotype.Service;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Override
    public boolean hasActivities(Long idRoom) {
        return false;
    }
}
