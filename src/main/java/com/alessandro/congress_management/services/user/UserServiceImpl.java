package com.alessandro.congress_management.services.user;

import com.alessandro.congress_management.dto.user_manager.CreateUserCommand;
import com.alessandro.congress_management.dto.user_manager.UpdateUserRequest;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.RoleRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final RoleRepository  roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserEntity createUser(CreateUserCommand command) throws DuplicatedEntityException {
        validateUniqueFields(command.username(), command.email(), command.identificationNumber(), null);

        RoleEntity role = findRole(command.roleName());

        UserEntity user = buildUser(command, role);
        user.setPassword(passwordEncoder.encode(command.password()));
        user.setIsActive(true);

        return userRepository.save(user);
    }


    @Override
    public UserEntity createInactiveUser(CreateUserCommand command) throws DuplicatedEntityException {
        validateUniqueFields(command.username(), command.email(), command.identificationNumber(), null);

        RoleEntity role = findRole(command.roleName());

        UserEntity user = buildUser(command, role);
        user.setPassword(null);
        user.setIsActive(false);

        return userRepository.save(user);
    }


    @Override
    public UserEntity getUserById(Long idUser) throws NotFoundException {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + idUser));
    }


    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }


    @Override
    public List<UserEntity> findActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }


    @Override
    public void setUserActiveStatus(Long idUser, boolean isActive) throws NotFoundException {
        UserEntity user = userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        user.setIsActive(isActive);
        userRepository.save(user);
    }


    @Override
    public UserEntity updateUser(Long idUser, UpdateUserRequest command)
            throws NotFoundException, DuplicatedEntityException {
        UserEntity existing = userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + idUser));

        validateUniqueFields(command.getUsername(), command.getEmail(),
                command.getIdentificationNumber(), idUser);

        existing.setEmail(command.getEmail());
        existing.setFullName(command.getFullName());
        existing.setPhoneNumber(command.getPhoneNumber());
        existing.setOrganization(command.getOrganization());
        existing.setUsername(command.getUsername());
        existing.setIdentificationNumber(command.getIdentificationNumber());

        return userRepository.save(existing);
    }



    @Override
    public void changeUserPassword(Long idUser, String newPassword) throws NotFoundException {
        UserEntity user = userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    private UserEntity buildUser(CreateUserCommand command, RoleEntity role) {
        UserEntity user = new UserEntity();
        user.setUsername(command.username());
        user.setEmail(command.email());
        user.setFullName(command.fullName());
        user.setPhoneNumber(command.phoneNumber());
        user.setOrganization(command.organization());
        user.setIdentificationNumber(command.identificationNumber());
        user.setRole(role);
        return user;
    }

    private RoleEntity findRole(String roleName) throws DuplicatedEntityException {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new DuplicatedEntityException("El rol especificado no existe"));
    }

    private void validateUniqueFields(String username, String email,
                                      String identificationNumber,
                                      Long currentUserId) throws DuplicatedEntityException {
        if (userRepository.existsByUsernameAndIdUserNot(username, currentUserId)) {
            throw new DuplicatedEntityException("El username ya está en uso");
        }
        if (userRepository.existsByEmailAndIdUserNot(email, currentUserId)) {
            throw new DuplicatedEntityException("El email ya está registrado");
        }
        if (userRepository.existsByIdentificationNumberAndIdUserNot(identificationNumber, currentUserId)) {
            throw new DuplicatedEntityException("El número de identificación ya está registrado");
        }
    }
}