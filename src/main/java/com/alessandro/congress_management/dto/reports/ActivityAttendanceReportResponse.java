package com.alessandro.congress_management.dto.reports;

import lombok.Value;

import java.util.List;

@Value
public class ActivityAttendanceReportResponse {
    Long idCongress;
    String congressName;
    List<ActivityAttendanceDto> activities;
    int totalAttendanceRecords;

    @Value
    public static class ActivityAttendanceDto {
        Long idActivity;
        String activityName;
        String roomName;
        String startTime;
        String endTime;
        int attendanceCount;
    }
}