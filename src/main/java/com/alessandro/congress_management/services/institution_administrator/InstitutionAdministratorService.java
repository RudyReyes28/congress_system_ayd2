package com.alessandro.congress_management.services.institution_administrator;

import com.alessandro.congress_management.dto.institution_administrator.CreateInstitutionAdministratorRequest;
import com.alessandro.congress_management.dto.institution_administrator.InstitutionNameRequest;
import com.alessandro.congress_management.dto.institution_administrator.UserInstitutionResponse;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;

import java.util.List;

public interface InstitutionAdministratorService {

    InstitutionAdministratorEntity createInstitutionAdministrator(CreateInstitutionAdministratorRequest request) throws NotFoundException;

    InstitutionAdministratorEntity findInstitutionAdministratorByIdAdministrator(Long idAdministrator) throws NotFoundException;

    boolean isUserAdminOfInstitution(Long idUser, Long idInstitution) throws NotFoundException;

    List<UserInstitutionResponse> getAdministratorsByInstitution(InstitutionNameRequest request) throws NotFoundException;


}
