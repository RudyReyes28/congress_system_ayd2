package com.alessandro.congress_management.services.congressadministrator;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congressadministrator.UserCongressResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressAdministratorEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.services.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CongressAdministratorServiceImpl implements CongressAdministratorService{
    private final CongressAdministratorRepository congressAdminRepository;
    private final UserService userService;
    private final CongressRepository congressRepository;

    public CongressAdministratorServiceImpl(CongressAdministratorRepository congressAdminRepository, UserService userService, CongressRepository congressRepository) {
        this.congressAdminRepository = congressAdminRepository;
        this.userService = userService;
        this.congressRepository = congressRepository;
    }

    @Override
    public CongressAdministratorEntity findCongressAdminEntityById(Long idCongressAdmin) throws NotFoundException {
        return null;
    }

    @Override
    public CongressAdministratorEntity assignAdministratorToCongress(Long idUser, CongressEntity congress) throws NotFoundException, BusinessRuleException {
        //Obtener el usuario
        UserEntity user = userService.getUserById(idUser);
        //Revisar que el usuario no sea ya admin del congreso
        if(congressAdminRepository.existsByUserAndCongress(user, congress)){
            throw new BusinessRuleException("User with id: " + idUser + " is already an admin of congress with id: " + congress.getIdCongress());
        }
        //Crear el admin del congreso
        CongressAdministratorEntity congressAdmin = new CongressAdministratorEntity();
        congressAdmin.setUser(user);
        congressAdmin.setCongress(congress);
        //Guardar el admin del congreso
        return congressAdminRepository.save(congressAdmin);
    }

    @Override
    public List<CongressResponse> getCongressesByAdministrator(Long idUser) throws NotFoundException {
        List<CongressAdministratorEntity> congressAdmins = congressAdminRepository.findByUser_IdUser(idUser);
        if(congressAdmins.isEmpty()){
            throw new NotFoundException("No congresses found for user with id: " + idUser);
        }
        return congressAdmins.stream().map(ca -> CongressResponse.fromEntity(ca.getCongress())).toList();
    }

    @Override
    public List<UserCongressResponse> getAdministratorsByCongress(Long idCongress) throws NotFoundException {
        List<CongressAdministratorEntity> congressAdmins = congressAdminRepository.findByCongress_IdCongress(idCongress);
        if(congressAdmins.isEmpty()){
            throw new NotFoundException("No administrators found for congress with id: " + idCongress);
        }
        return congressAdmins.stream().map(UserCongressResponse::fromEntity).toList();
    }

    @Override
    public void removeAdministratorFromCongress(Long idUser, CongressEntity Congress) throws NotFoundException, BusinessRuleException {

        //Obtener el usuario
        UserEntity user = userService.getUserById(idUser);
        //Verificar que no se esté intentando remover al último administrador del congreso
        if(congressAdminRepository.countByCongress_IdCongress(Congress.getIdCongress()) <= 1){
            throw new BusinessRuleException("Cannot remove the last administrator of the congress");
        }

        //Remover el usuario del congreso
        CongressAdministratorEntity congressAdmin = congressAdminRepository.findByUser_IdUserAndCongress_IdCongress(user.getIdUser(), Congress.getIdCongress())
                .orElseThrow(() -> new NotFoundException("User  is not an admin of congress" ));

        congressAdminRepository.delete(congressAdmin);
    }
}
