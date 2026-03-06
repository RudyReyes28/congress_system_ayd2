package com.alessandro.congress_management.services.activitypresenter;

import com.alessandro.congress_management.dto.activitypresenter.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;

import java.util.List;

public interface ActivityPresenterService {
    ActivityPresenterResponse assignExistingUser(ActivityPresenterRequest request, Long idActivity) throws NotFoundException, BusinessRuleException;

    ActivityPresenterResponse assignInvitedUser(AssignInviteUserActivityRequest request, Long idActivity) throws NotFoundException, BusinessRuleException, DuplicatedEntityException;

     void removePresenter(Long idActivityPresenter) throws NotFoundException, BusinessRuleException;

     List<DetailActivityPresenterResponse> getPresentersByActivity(Long idActivity);

    //void assignFromApprovedSubmission( Long idActivity, Long idUser);
    ActivityPresenterEntity findById(Long idActivityPresenter) throws NotFoundException;

    List<EligibleUsersActivityResponse> getEligibleRegisteredUsers(Long idActivity) throws NotFoundException;

    List<EligibleUsersActivityResponse> getEligibleInvitedUsers(Long activityId) throws NotFoundException;

}
