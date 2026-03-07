package com.alessandro.congress_management.services.user_manager;

import com.alessandro.congress_management.dto.institution_administrator.CreateInstitutionAdministratorRequest;
import com.alessandro.congress_management.dto.user_manager.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.services.institution_administrator.InstitutionAdministratorService;
import com.alessandro.congress_management.services.invitation.UserInvitationService;
import com.alessandro.congress_management.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class UserManagerServiceImpl implements UserManagerService {

    private static final String ADMIN_ROLE_NAME = "ADMIN_SYSTEM";

    private final UserRepository userRepository;
    private final UserService userService;
    private final InstitutionAdministratorService institutionAdministratorService;
    private final UserInvitationService invitationService;

    public UserManagerServiceImpl(UserRepository userRepository, UserService userService, InstitutionAdministratorService institutionAdministratorService, UserInvitationService invitationService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.institutionAdministratorService = institutionAdministratorService;
        this.invitationService = invitationService;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse createUserByAdmin(CreateUserRequest createUserRequest)
            throws DuplicatedEntityException, NotFoundException {

        CreateUserCommand command = new CreateUserCommand(
                createUserRequest.getUsername(),
                createUserRequest.getEmail(),
                null,
                createUserRequest.getFullName(),
                createUserRequest.getPhoneNumber(),
                createUserRequest.getOrganization(),
                createUserRequest.getIdentificationNumber(),
                createUserRequest.getRoleName()
        );

        // Creamos el usuario inactivo
        UserEntity user = userService.createInactiveUser(command);

        // Eviamos el correo de invitacion para activar la cuenta
        invitationService.sendInvitation(user.getIdUser());

        return new UserResponse(user);
    }



    @Override
    public void resendInvitation(Long idUser) throws NotFoundException, BusinessRuleException {
        UserEntity user = userService.getUserById(idUser);
        if (user.getIsActive()) {
            throw new BusinessRuleException("El usuario ya tiene su cuenta activada.");
        }
        invitationService.sendInvitation(idUser);
    }


    @Override
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream().map(UserResponse::new).toList();
    }

    @Override
    public void setUserActiveStatus(Long idUser, UpdateStatusUser updateStatusUser)
            throws DuplicatedEntityException, NotFoundException {
        if (!updateStatusUser.isActive()
                && userRepository.countByRole_RoleNameAndIsActive(ADMIN_ROLE_NAME, true) == 1) {
            UserEntity user = userService.getUserById(idUser);
            if (user.getRole().getRoleName().equals(ADMIN_ROLE_NAME) && user.getIsActive()) {
                throw new DuplicatedEntityException("No se puede desactivar el único administrador activo");
            }
        }
        userService.setUserActiveStatus(idUser, updateStatusUser.isActive());
    }

    @Override
    public UserResponse updateUserByAdmin(Long idUser, UpdateUserRequest updateUserRequest)
            throws DuplicatedEntityException, NotFoundException {
        UserEntity updatedUser = userService.updateUser(idUser, updateUserRequest);
        return new UserResponse(updatedUser);
    }

    @Override
    public void changeUserPasswordByAdmin(Long idUser, UpdateUserPassword updatePassword)
            throws NotFoundException {
        userService.changeUserPassword(idUser, updatePassword.getNewPassword());
    }

    @Override
    @Transactional
    public UserCongressAdminResponse createCongressAdmin(CreateCongressAdminRequest request)
            throws DuplicatedEntityException, NotFoundException {

        CreateUserCommand command = new CreateUserCommand(
                request.getUsername(),
                request.getEmail(),
                null,  // no password on creation
                request.getFullName(),
                request.getPhoneNumber(),
                request.getOrganization(),
                request.getIdentificationNumber(),
                "ADMIN_CONGRESS"
        );

        UserEntity user = userService.createInactiveUser(command);

        InstitutionAdministratorEntity institutionAdmin =
                institutionAdministratorService.createInstitutionAdministrator(
                        new CreateInstitutionAdministratorRequest(request.getIdInstitution(), user.getIdUser()));

        // Send invitation email
        invitationService.sendInvitation(user.getIdUser());

        return UserCongressAdminResponse.fromEntity(institutionAdmin);
    }
}