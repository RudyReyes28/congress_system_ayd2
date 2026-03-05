package com.alessandro.congress_management.dto.submissions.evaluation;

import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CallSummaryResponse {
    Long idCall;
    String callName;
    String description;
    boolean open;
    LocalDateTime openDate;
    LocalDateTime closeDate;

    public static CallSummaryResponse fromEntity(CallForPapersEntity call) {
        return new CallSummaryResponse(
                call.getIdCall(),
                call.getCallName(),
                call.getDescription(),
                call.getIsOpen(),
                call.getOpenDate(),
                call.getCloseDate()
        );
    }
}
