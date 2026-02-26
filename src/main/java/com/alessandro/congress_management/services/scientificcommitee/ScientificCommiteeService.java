package com.alessandro.congress_management.services.scientificcommitee;

import com.alessandro.congress_management.dto.scientificcommitee.EligibleUserCommitteeResponse;
import com.alessandro.congress_management.dto.scientificcommitee.ScientificCommiteeResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.util.List;

public interface  ScientificCommiteeService {
    ScientificCommiteeResponse createScientificCommiteeMember(Long congressId, Long userId) throws NotFoundException, BusinessRuleException;

    List<ScientificCommiteeResponse> getScientificCommiteeMembersByCongressId(Long congressId);

    void deleteScientificCommiteeMember(Long congressId, Long userId) throws NotFoundException;

    List<EligibleUserCommitteeResponse> getEligibleUsersForCommittee(Long congressId) throws NotFoundException;

}
