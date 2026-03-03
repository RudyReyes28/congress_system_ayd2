package com.alessandro.congress_management.dto.activitypresenter;

import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;
import lombok.Value;

@Value
public class ActivityPresenterResponse {
    Long idActivityPresenter;
    String activityName;
    String presenterName;
    Boolean invitedSpeaker;
    Boolean mainAuthor;

        public static ActivityPresenterResponse fromEntity(ActivityPresenterEntity activityPresenter) {
            return new ActivityPresenterResponse(
                    activityPresenter.getIdActivityPresenter(),
                    activityPresenter.getActivity().getActivityName(),
                    activityPresenter.getUser().getFullName(),
                    activityPresenter.getIsInvitedSpeaker(),
                    activityPresenter.getIsMainAuthor()
            );
        }
}
