package com.alessandro.congress_management.services.user_manager;

import com.alessandro.congress_management.dto.institution_administrator.CreateInstitutionAdministratorRequest;
import com.alessandro.congress_management.dto.user_manager.*;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.repositories.authenticate.RoleRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.services.institution_administrator.InstitutionAdministratorService;
import com.alessandro.congress_management.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public UserManagerServiceImpl(UserRepository userRepository, UserService userService, InstitutionAdministratorService institutionAdministratorService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.institutionAdministratorService = institutionAdministratorService;
    }


    @Override
    public UserResponse createUserByAdmin(CreateUserRequest createUserRequest) throws DuplicatedEntityException {
        CreateUserCommand command = new CreateUserCommand(
                createUserRequest.getUsername(),
                createUserRequest.getEmail(),
                createUserRequest.getPassword(),
                createUserRequest.getFullName(),
                createUserRequest.getPhoneNumber(),
                createUserRequest.getOrganization(),
                createUserRequest.getIdentificationNumber(),
                createUserRequest.getRoleName()
        );

        UserEntity user = userService.createUser(command);

        return new UserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        return users.stream().map(UserResponse::new).toList();
    }

    @Override
    public void setUserActiveStatus(Long idUser, UpdateStatusUser updateStatusUser) throws DuplicatedEntityException,NotFoundException {
        //Verificar que no haya solo un administrador activo
        if ( !updateStatusUser.isActive() && userRepository.countByRole_RoleNameAndIsActive(ADMIN_ROLE_NAME, true) == 1) {
            UserEntity user = userService.getUserById(idUser);
            if (user.getRole().getRoleName().equals(ADMIN_ROLE_NAME) && user.getIsActive()) {
                throw new DuplicatedEntityException("No se puede eliminar el único administrador activo");
            }
        }
        userService.setUserActiveStatus(idUser, updateStatusUser.isActive());

    }

    @Override
    public UserResponse updateUserByAdmin(Long idUser, UpdateUserRequest updateUserRequest) throws DuplicatedEntityException, NotFoundException {
        UserEntity updatedUser = userService.updateUser(idUser, updateUserRequest);
        return new UserResponse(updatedUser);
    }

    @Override
    public void changeUserPasswordByAdmin(Long idUser,UpdateUserPassword updatePassword) throws NotFoundException {
        userService.changeUserPassword(idUser, updatePassword.getNewPassword());
    }

    @Override
    public UserCongressAdminResponse createCongressAdmin(CreateCongressAdminRequest createCongressAdminRequest) throws DuplicatedEntityException, NotFoundException {
        CreateUserCommand command = new CreateUserCommand(
                createCongressAdminRequest.getUsername(),
                createCongressAdminRequest.getEmail(),
                createCongressAdminRequest.getPassword(),
                createCongressAdminRequest.getFullName(),
                createCongressAdminRequest.getPhoneNumber(),
                createCongressAdminRequest.getOrganization(),
                createCongressAdminRequest.getIdentificationNumber(),
                "ADMIN_CONGRESS"
        );

        UserEntity user = userService.createUser(command);

        InstitutionAdministratorEntity institutionAdmin = institutionAdministratorService.createInstitutionAdministrator(
                new CreateInstitutionAdministratorRequest(createCongressAdminRequest.getIdInstitution(), user.getIdUser())
        );

        return UserCongressAdminResponse.fromEntity(institutionAdmin);
    }


}
