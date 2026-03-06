package com.alessandro.congress_management.dto.institution_administrator;

import lombok.Value;

@Value
public class CreateInstitutionAdministratorRequest {
    Long idInstitution;
    Long idUser;
}
