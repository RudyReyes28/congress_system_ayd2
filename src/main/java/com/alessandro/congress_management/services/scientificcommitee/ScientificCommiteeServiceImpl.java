package com.alessandro.congress_management.services.scientificcommitee;

import com.alessandro.congress_management.dto.scientificcommitee.EligibleUserCommitteeResponse;
import com.alessandro.congress_management.dto.scientificcommitee.ScientificCommiteeResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.congress_management.ScientificCommiteeEntity;
import com.alessandro.congress_management.repositories.scientificcommitee.ScientificCommiteeRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScientificCommiteeServiceImpl implements ScientificCommiteeService {

    private final ScientificCommiteeRepository scientificCommiteeRepository;
    private final CongressService congressService;
    private final UserService userService;

    public ScientificCommiteeServiceImpl(ScientificCommiteeRepository scientificCommiteeRepository, CongressService congressService, UserService userService) {
        this.scientificCommiteeRepository = scientificCommiteeRepository;
        this.congressService = congressService;
        this.userService = userService;
    }


    @Override
    public ScientificCommiteeResponse createScientificCommiteeMember(Long congressId, Long userId) throws NotFoundException, BusinessRuleException {
        //Obtener el congreso y el usuario
        CongressEntity congress = congressService.findCongressEntityById(congressId);

        //Validar que el congreso este activo
        if(congress.getIsActive() == Boolean.FALSE) {
            throw new BusinessRuleException("Cannot assign a scientific committee member to an inactive congress");
        }

        //Obtenemos el usuario
        UserEntity user = userService.getUserById(userId);

        //Validar que el usuario este activo
        if(user.getIsActive() == Boolean.FALSE) {
            throw new BusinessRuleException("Cannot assign an inactive user as a scientific committee member");
        }

        //Validar que el usuario no sea ya miembro del comite cientifico del congreso
        if(scientificCommiteeRepository.existsByCongress_IdCongressAndUser_IdUser(congressId, userId)) {
            throw new BusinessRuleException("The user is already a member of the scientific committee of this congress");
        }

        //Crear el miembro del comite cientifico
        ScientificCommiteeEntity committeeMember = new ScientificCommiteeEntity();
        committeeMember.setCongress(congress);
        committeeMember.setUser(user);

        ScientificCommiteeEntity savedCommitteeMember = scientificCommiteeRepository.save(committeeMember);

        return ScientificCommiteeResponse.fromEntity(savedCommitteeMember);

    }

    @Override
    public List<ScientificCommiteeResponse> getScientificCommiteeMembersByCongressId(Long congressId) {
        List<ScientificCommiteeEntity> committeeMembers = scientificCommiteeRepository.findAllByCongress_IdCongress(congressId);
        return committeeMembers.stream().map(ScientificCommiteeResponse::fromEntity).toList();
    }

    @Override
    public void deleteScientificCommiteeMember(Long congressId, Long userId) throws NotFoundException {
        ScientificCommiteeEntity member = scientificCommiteeRepository
                .findByCongress_IdCongressAndUser_IdUser(congressId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Scientific committee member not found"));

        scientificCommiteeRepository.delete(member);
    }

    @Override
    public List<EligibleUserCommitteeResponse> getEligibleUsersForCommittee(Long congressId) throws NotFoundException {

        //Verificar que el congreso exista
        CongressEntity congress = congressService.findCongressEntityById(congressId);

        // Obtener usuarios activos
        List<UserEntity> activeUsers = userService.findActiveUsers();

        // Filtrar los que ya están en el comite
        List<Long> existingMembers = scientificCommiteeRepository.findUserIdsByCongressId(congressId);

        return activeUsers.stream()
                .filter(user -> !existingMembers.contains(user.getIdUser()))
                .map(EligibleUserCommitteeResponse::fromEntity)
                .toList();

    }
}
