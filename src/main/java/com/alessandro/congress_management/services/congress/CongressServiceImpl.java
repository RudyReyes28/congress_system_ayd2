package com.alessandro.congress_management.services.congress;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congress.CreateCongressRequest;
import com.alessandro.congress_management.dto.congress.UpdateCongressRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.services.congressadministrator.CongressAdministratorService;
import com.alessandro.congress_management.services.institution.InstitutionService;
import com.alessandro.congress_management.services.institution_administrator.InstitutionAdministratorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class CongressServiceImpl implements CongressService{

    private final CongressRepository congressRepository;
    private final InstitutionAdministratorService institutionAdminService;
    private final CongressAdministratorService congressAdminService;
    private final RegistrationRepository registrationRepository;

    public CongressServiceImpl(CongressRepository congressRepository, InstitutionAdministratorService institutionAdminService, CongressAdministratorService congressAdminService, RegistrationRepository registrationRepository) {
        this.congressRepository = congressRepository;
        this.institutionAdminService = institutionAdminService;
        this.congressAdminService = congressAdminService;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public CongressResponse createCongress(CreateCongressRequest request) throws DuplicatedEntityException, BusinessRuleException, NotFoundException {
        //Validar que no exista un congreso con el mismo nombre
        if(congressRepository.existsByCongressName(request.getCongressName())){
            throw new DuplicatedEntityException("A congress with the same name already exists");
        }

        //Validar que las fechas sean coherentes
        if(request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleException("Start date cannot be after end date");
        }

        //Obtenemos la institución del administrador para asignarla al congreso
        InstitutionAdministratorEntity institutionAdmin = institutionAdminService.findInstitutionAdministratorByIdAdministrator(request.getIdCongressManager());

        //Verificar que la institucion este activa
        if(institutionAdmin.getInstitution().getIsActive() == Boolean.FALSE){
            throw new BusinessRuleException("The institution of the congress manager must be active");
        }

        //Crear el congreso
        CongressEntity congress = request.toEntity(institutionAdmin.getInstitution());
        CongressEntity savedCongress = congressRepository.save(congress);

        //Asignar el administrador al congreso
        congressAdminService.assignAdministratorToCongress(request.getIdCongressManager(), savedCongress.getIdCongress());

        return CongressResponse.fromEntity(savedCongress);
    }

    @Override
    public CongressResponse updateCongress(Long idCongress, UpdateCongressRequest request) throws BusinessRuleException, NotFoundException {
        //Validar que las fechas sean coherentes
        if(request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleException("Start date cannot be after end date");
        }

        //Validar que no exista otro congreso con el mismo nombre
        if(congressRepository.existsByCongressNameAndIdCongressNot(request.getCongressName(), idCongress)){
            throw new BusinessRuleException("Another congress with the same name already exists");
        }

        CongressEntity congress = findCongressEntityById(idCongress);
        //Validar precios si hay inscripciones
        if (request.getPrice().compareTo(congress.getPrice()) != 0 &&
                registrationRepository.existsByCongress_IdCongress(idCongress)) {
            throw new BusinessRuleException("Cannot change the price of the congress because there are already registrations");
        }

        request.applyToEntity(congress);

        CongressEntity updatedCongress = congressRepository.save(congress);
        return CongressResponse.fromEntity(updatedCongress);
    }

    @Override
    public CongressEntity findCongressEntityById(Long idCongress) throws NotFoundException {
        return congressRepository.findById(idCongress)
                .orElseThrow(() -> new NotFoundException("Congress not found with id: " + idCongress));
    }

    @Override
    public List<CongressResponse> getAllCongresses() {
        List<CongressEntity> congresses = congressRepository.findAll();
        return congresses.stream()
                .map(CongressResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CongressResponse> getActiveCongresses() {
        List<CongressEntity> congresses = congressRepository.findByIsActiveTrue();
        return congresses.stream()
                .map(CongressResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CongressResponse> getCongressesByAdmin(Long idUser) throws NotFoundException {
        List<CongressEntity> congresses = congressAdminService.getCongressesByAdministrator(idUser);
        return congresses.stream()
                .map(CongressResponse::fromEntity)
                .toList();
    }
}
