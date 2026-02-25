package com.alessandro.congress_management.dto.congressadministrator;

import com.alessandro.congress_management.models.congress_management.CongressAdministratorEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class UserCongressResponse {
    Long idUser;
    String username;
    String email;
    String fullName;
    String phoneNumber;
    String organization;
    String identificationNumber;
    String institutionName;
    LocalDateTime assignedAt;

    public static UserCongressResponse fromEntity (CongressAdministratorEntity congressAdmin) {
        return new UserCongressResponse(
                congressAdmin.getUser().getIdUser(),
                congressAdmin.getUser().getUsername(),
                congressAdmin.getUser().getEmail(),
                congressAdmin.getUser().getFullName(),
                congressAdmin.getUser().getPhoneNumber(),
                congressAdmin.getUser().getOrganization(),
                congressAdmin.getUser().getIdentificationNumber(),
                congressAdmin.getCongress().getInstitution().getInstitutionName(),
                congressAdmin.getCreatedAt()
        );
    }
}
