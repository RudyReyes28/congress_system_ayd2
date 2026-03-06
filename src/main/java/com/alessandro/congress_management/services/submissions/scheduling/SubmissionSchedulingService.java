package com.alessandro.congress_management.services.submissions.scheduling;

import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.submissions.scheduling.EvaluationSubmissionDetails;
import com.alessandro.congress_management.dto.submissions.scheduling.ScheduleSubmissionsRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.util.List;

public interface SubmissionSchedulingService  {

    List<EvaluationSubmissionDetails> getEvaluationSubmissionsByIdCall(Long idCall) throws NotFoundException;

    ActivityResponse scheduleSubmissions(Long idSubmission, ScheduleSubmissionsRequest request) throws NotFoundException, BusinessRuleException;

    void cancelScheduling(Long idSubmission) throws NotFoundException;

}
