package com.alessandro.congress_management.services.institution;

import com.alessandro.congress_management.dto.institution.InstitutionResponse;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;

import java.util.List;

public interface InstitutionService {
    List<InstitutionEntity> getAllInstitutions();

    List<InstitutionEntity> getActiveInstitutions();

    InstitutionEntity getInstitutionById(Long id) throws NotFoundException;
}
