package com.alessandro.congress_management.dto.activitypresenter;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class ActivityPresenterRequest {
    @NotNull(message = "User ID cannot be null")
    Long idUser;
    @NotNull(message = "Invited speaker status cannot be null")
    Boolean invitedSpeaker;
}
