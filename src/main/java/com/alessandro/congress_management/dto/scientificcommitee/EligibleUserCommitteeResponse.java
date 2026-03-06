package com.alessandro.congress_management.dto.scientificcommitee;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import lombok.Value;

@Value
public class EligibleUserCommitteeResponse {
    Long idUser;
    String fullName;
    String organization;

    public static EligibleUserCommitteeResponse fromEntity(UserEntity userEntity) {
        return new EligibleUserCommitteeResponse(
                userEntity.getIdUser(),
                userEntity.getFullName(),
                userEntity.getOrganization()
        );
    }
}
