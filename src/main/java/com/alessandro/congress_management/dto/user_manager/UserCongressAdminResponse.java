package com.alessandro.congress_management.dto.user_manager;

import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import lombok.Value;

@Value
public class UserCongressAdminResponse {
    String email;
    String fullName;
    String phoneNumber;
    String organization;
    String username;
    String identificationNumber;
    String institutionName;

    public static UserCongressAdminResponse fromEntity(InstitutionAdministratorEntity user) {
        return new UserCongressAdminResponse(
                user.getUser().getEmail(),
                user.getUser().getFullName(),
                user.getUser().getPhoneNumber(),
                user.getUser().getOrganization(),
                user.getUser().getUsername(),
                user.getUser().getIdentificationNumber(),
                user.getInstitution().getInstitutionName()
        );
    }
}
