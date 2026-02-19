package com.alessandro.congress_management.services.institution;

import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.institution.InstitutionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstitutionServiceImpl implements InstitutionService{

    private final InstitutionRepository institutionRepository;

    public InstitutionServiceImpl(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @Override
    public List<InstitutionEntity> getAllInstitutions() {
        return institutionRepository.findAll();
    }

    @Override
    public List<InstitutionEntity> getActiveInstitutions() {
        return institutionRepository.findByIsActiveTrue().orElseThrow(() -> new RuntimeException("No active institutions found"));
    }

    @Override
    public InstitutionEntity getInstitutionById(Long id) throws NotFoundException {
        return institutionRepository.findById(id).orElseThrow(() -> new NotFoundException("Institution not found"));
    }
}
