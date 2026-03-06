package com.alessandro.congress_management.services.institution;

import com.alessandro.congress_management.dto.institution.CreateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.InstitutionResponse;
import com.alessandro.congress_management.dto.institution.UpdateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.UpdateStatusInstitutionRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;

import java.util.List;

public interface InstitutionService {
    List<InstitutionEntity> getAllInstitutions();

    List<InstitutionEntity> getActiveInstitutions();

    InstitutionEntity getInstitutionById(Long id) throws NotFoundException;

    InstitutionResponse createInstitution(CreateInstitutionRequest request) throws DuplicatedEntityException;

    InstitutionResponse updateInstitution(Long id, UpdateInstitutionRequest request) throws NotFoundException, DuplicatedEntityException;

    void uptateStatusInstitution(Long id, UpdateStatusInstitutionRequest updateStatus) throws NotFoundException, BusinessRuleException;


}
