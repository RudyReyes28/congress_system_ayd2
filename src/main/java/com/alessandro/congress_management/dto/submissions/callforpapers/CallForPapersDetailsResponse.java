package com.alessandro.congress_management.dto.submissions.callforpapers;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CallForPapersDetailsResponse {
    Long idCall;
    String callName;
    String description;
    LocalDateTime openDate;
    LocalDateTime closeDate;
    Boolean isOpen;
    CongressResponse congress;

    public static CallForPapersDetailsResponse fromEntity(CallForPapersEntity entity) {
        return new CallForPapersDetailsResponse(
                entity.getIdCall(),
                entity.getCallName(),
                entity.getDescription(),
                entity.getOpenDate(),
                entity.getCloseDate(),
                entity.getIsOpen(),
                CongressResponse.fromEntity(entity.getCongress())
        );
    }


}
