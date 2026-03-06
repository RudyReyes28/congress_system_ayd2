package com.alessandro.congress_management.services.institution_administrator;

import com.alessandro.congress_management.dto.institution_administrator.CreateInstitutionAdministratorRequest;
import com.alessandro.congress_management.dto.institution_administrator.InstitutionNameRequest;
import com.alessandro.congress_management.dto.institution_administrator.UserInstitutionResponse;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.congress_management.InstitutionAdministratorRepository;
import com.alessandro.congress_management.services.institution.InstitutionService;
import com.alessandro.congress_management.services.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstitutionAdministratorServiceImpl implements InstitutionAdministratorService {
    private final InstitutionAdministratorRepository institutionAdministratorRepository;
    private final InstitutionService institutionService;
    private final UserService userService;

    public InstitutionAdministratorServiceImpl(InstitutionAdministratorRepository institutionAdministratorRepository, InstitutionService institutionService, UserService userService) {
        this.institutionAdministratorRepository = institutionAdministratorRepository;
        this.institutionService = institutionService;
        this.userService = userService;
    }

    @Override
    public InstitutionAdministratorEntity createInstitutionAdministrator(CreateInstitutionAdministratorRequest request) throws NotFoundException {
        // Validate institution and user existence
        InstitutionEntity institution = institutionService.getInstitutionById(request.getIdInstitution());
        UserEntity user = userService.getUserById(request.getIdUser());

        // Create and save the institution administrator
        InstitutionAdministratorEntity institutionAdministrator = new InstitutionAdministratorEntity();
        institutionAdministrator.setInstitution(institution);
        institutionAdministrator.setUser(user);

        return institutionAdministratorRepository.save(institutionAdministrator);
    }

    @Override
    public InstitutionAdministratorEntity findInstitutionAdministratorByIdAdministrator(Long idAdministrator) throws NotFoundException {
        return institutionAdministratorRepository.findByUser_IdUser(idAdministrator)
                .orElseThrow(() -> new NotFoundException("Institution administrator not found with id: " + idAdministrator));
    }

    @Override
    public boolean isUserAdminOfInstitution(Long idUser, Long idInstitution) throws NotFoundException {
        return institutionAdministratorRepository.existsByUser_IdUserAndInstitution_IdInstitution(idUser, idInstitution);
    }

    @Override
    public List<UserInstitutionResponse> getAdministratorsByInstitution(InstitutionNameRequest request) throws NotFoundException {
        List<InstitutionAdministratorEntity> institutionAdmins = institutionAdministratorRepository.findByInstitution_InstitutionName(request.getInstitutionName());


        return institutionAdmins.stream()
                .map(UserInstitutionResponse::fromEntity)
                .toList();

    }
}
