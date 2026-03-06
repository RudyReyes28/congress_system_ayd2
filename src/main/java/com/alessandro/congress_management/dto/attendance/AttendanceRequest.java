package com.alessandro.congress_management.dto.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class AttendanceRequest {
    @NotNull(message = "User cannot be null")
    Long idUser;
}
