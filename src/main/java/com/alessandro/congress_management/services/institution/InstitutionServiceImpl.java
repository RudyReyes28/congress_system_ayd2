package com.alessandro.congress_management.services.institution;

import com.alessandro.congress_management.dto.institution.CreateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.InstitutionResponse;
import com.alessandro.congress_management.dto.institution.UpdateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.UpdateStatusInstitutionRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.institution.InstitutionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstitutionServiceImpl implements InstitutionService{

    private final InstitutionRepository institutionRepository;
    private final CongressRepository congressRepository;

    public InstitutionServiceImpl(InstitutionRepository institutionRepository, CongressRepository congressRepository) {
        this.institutionRepository = institutionRepository;
        this.congressRepository = congressRepository;
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

    @Override
    public InstitutionResponse createInstitution(CreateInstitutionRequest request) throws DuplicatedEntityException {
       //Verificar que no exista una institución con el mismo nombre
        if (institutionRepository.existsByInstitutionName(request.getInstitutionName())) {
            throw new DuplicatedEntityException("Institution with the same name already exists");
        }

        if (institutionRepository.existsByContactEmail(request.getContactEmail())) {
            throw new DuplicatedEntityException("Institution with the same contact email already exists");
        }

        InstitutionEntity institution = request.toEntity();

        InstitutionEntity savedInstitution = institutionRepository.save(institution);
        return InstitutionResponse.fromEntity(savedInstitution);
    }

    @Override
    public InstitutionResponse updateInstitution(Long id, UpdateInstitutionRequest request) throws NotFoundException, DuplicatedEntityException {
        //Obtenemos la institución a actualizar
        InstitutionEntity institution = institutionRepository.findById(id).orElseThrow(() -> new NotFoundException("Institution not found"));

        //Verificar que no exista una institución con el mismo nombre (excluyendo la institución actual)
        if(institutionRepository.existsByInstitutionNameAndIdInstitutionNot(request.getInstitutionName(), id)) {
            throw new DuplicatedEntityException("Institution with the same name already exists");
        }

        //Actualizamos los campos de la institucion
        institution.setInstitutionName(request.getInstitutionName());
        institution.setDescription(request.getDescription());
        institution.setAddress(request.getAddress());
        institution.setContactEmail(request.getContactEmail());
        institution.setContactPhone(request.getContactPhone());

        InstitutionEntity updatedInstitution = institutionRepository.save(institution);
        return InstitutionResponse.fromEntity(updatedInstitution);
    }

    @Override
    public void uptateStatusInstitution(Long id, UpdateStatusInstitutionRequest updateStatus) throws NotFoundException, BusinessRuleException {
        InstitutionEntity institution = institutionRepository.findById(id).orElseThrow(() -> new NotFoundException("Institution not found"));
        if(!updateStatus.isActive() && congressRepository.existsByIsActiveTrueAndInstitution_IdInstitution(institution.getIdInstitution()) ) {
            throw new BusinessRuleException("Cannot deactivate institution with active congresses");
        }
        institution.setIsActive(updateStatus.isActive());
        institutionRepository.save(institution);
    }
}
