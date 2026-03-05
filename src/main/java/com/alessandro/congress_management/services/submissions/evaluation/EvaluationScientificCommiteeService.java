package com.alessandro.congress_management.services.submissions.evaluation;

import com.alessandro.congress_management.dto.submissions.evaluation.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.util.List;

public interface EvaluationScientificCommiteeService {

    List<CongressSummaryResponse> getMyCommitteeCongresses(Long userId) throws NotFoundException;

    List<CallSummaryResponse> getCallsByCongressForCommittee(Long congressId, Long userId) throws NotFoundException;

    List<SubmissionSummaryResponse> getSubmissionsByCallForCommittee(Long callId, Long userId) throws NotFoundException, BusinessRuleException;

    SubmissionDetailsResponse getSubmissionDetailsForCommittee(Long submissionId, Long userId) throws NotFoundException;

    EvaluationResponse evaluateSubmission(Long submissionId, Long evaluatorId, EvaluationRequest request) throws NotFoundException, BusinessRuleException;

    List<EvaluationDetailsResponse> getMyEvaluations(Long userId);


}
