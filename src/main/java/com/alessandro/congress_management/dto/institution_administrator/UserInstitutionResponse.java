package com.alessandro.congress_management.dto.institution_administrator;

import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import lombok.Value;

@Value
public class UserInstitutionResponse {
    Long idUser;
    String username;
    String email;
    String fullName;
    String phoneNumber;
    String organization;
    String identificationNumber;
    String institutionName;

        public static UserInstitutionResponse fromEntity (InstitutionAdministratorEntity institutionAdmin) {
            return new UserInstitutionResponse(
                    institutionAdmin.getUser().getIdUser(),
                    institutionAdmin.getUser().getUsername(),
                    institutionAdmin.getUser().getEmail(),
                    institutionAdmin.getUser().getFullName(),
                    institutionAdmin.getUser().getPhoneNumber(),
                    institutionAdmin.getUser().getOrganization(),
                    institutionAdmin.getUser().getIdentificationNumber(),
                    institutionAdmin.getInstitution().getInstitutionName()
            );
        }

}
