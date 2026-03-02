package com.alessandro.congress_management.dto.submissions.callforpapers;


import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CallForPapersRequest {
    @NotBlank(message = "Call name is required")
    String callName;
    @NotBlank(message = "Description is required")
    String description;
    @NotNull(message = "Open date is required")
    @FutureOrPresent(message = "Open date must be in the present or future")
    LocalDateTime openDate;
    @NotNull(message = "Close date is required")
    @FutureOrPresent(message = "Close date must be in the present or future")
    LocalDateTime closeDate;

    public CallForPapersEntity toEntity(CongressEntity congress) {
        CallForPapersEntity entity = new CallForPapersEntity();
        entity.setCallName(this.callName);
        entity.setDescription(this.description);
        entity.setOpenDate(this.openDate);
        entity.setCloseDate(this.closeDate);
        entity.setCongress(congress);
        return entity;
    }
}
