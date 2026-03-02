package com.alessandro.congress_management.dto.activitypresenter;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import lombok.Value;

@Value
public class EligibleUsersActivityResponse {
    Long idUser;
    String fullName;
    String email;
    String organization;
    String identificationNumber;

    public static EligibleUsersActivityResponse fromEntity(UserEntity user) {
        return new EligibleUsersActivityResponse(
                user.getIdUser(),
                user.getFullName(),
                user.getEmail(),
                user.getOrganization(),
                user.getIdentificationNumber()
        );
    }

}
