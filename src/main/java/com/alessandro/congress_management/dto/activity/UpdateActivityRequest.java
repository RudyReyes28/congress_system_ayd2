package com.alessandro.congress_management.dto.activity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class UpdateActivityRequest {
    @NotNull
    Long roomId;

    @NotBlank
    String activityName;

    @NotBlank
    String description;

    @NotNull
    LocalDateTime startTime;

    @NotNull
    LocalDateTime endTime;

    Integer maxCapacity;
}
