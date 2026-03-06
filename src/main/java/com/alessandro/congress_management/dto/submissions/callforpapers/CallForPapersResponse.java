package com.alessandro.congress_management.dto.submissions.callforpapers;

import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CallForPapersResponse {
    Long idCall;
    String callName;
    String description;
    LocalDateTime openDate;
    LocalDateTime closeDate;
    Boolean isOpen;

    public static CallForPapersResponse fromEntity(CallForPapersEntity entity) {
        return new CallForPapersResponse(
                entity.getIdCall(),
                entity.getCallName(),
                entity.getDescription(),
                entity.getOpenDate(),
                entity.getCloseDate(),
                entity.getIsOpen()
        );
    }
}
