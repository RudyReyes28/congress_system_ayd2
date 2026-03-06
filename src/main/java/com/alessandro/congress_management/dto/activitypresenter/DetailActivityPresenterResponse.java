package com.alessandro.congress_management.dto.activitypresenter;

import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;
import lombok.Value;

@Value
public class DetailActivityPresenterResponse {
    Long idActivityPresenter;
    Long idUser;
    String presenterName;
    String presenterEmail;
    String presenterIdentificationNumber;
    String presenterOrganization;
    Boolean invitedSpeaker;
    Boolean mainAuthor;

        public static DetailActivityPresenterResponse fromEntity(ActivityPresenterEntity activityPresenter) {
            return new DetailActivityPresenterResponse(
                    activityPresenter.getIdActivityPresenter(),
                    activityPresenter.getUser().getIdUser(),
                    activityPresenter.getUser().getFullName(),
                    activityPresenter.getUser().getEmail(),
                    activityPresenter.getUser().getIdentificationNumber(),
                    activityPresenter.getUser().getOrganization(),
                    activityPresenter.getIsInvitedSpeaker(),
                    activityPresenter.getIsMainAuthor()
            );
        }


}
