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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity createUser(CreateUserCommand createUserCommand) throws DuplicatedEntityException {

        // Validar datos
        validateUniqueFields(
                createUserCommand.username(),
                createUserCommand.email(),
                createUserCommand.identificationNumber(),
                null // null porque es creación, no actualización
        );

        RoleEntity defaultRole = roleRepository.findByRoleName(createUserCommand.roleName())
                .orElseThrow(() -> new DuplicatedEntityException("El rol especificado no existe"));

        // Hashear password
        String hashedPassword = passwordEncoder.encode(createUserCommand.password());

        // Crear entidad
        UserEntity user = new UserEntity();
        user.setEmail(createUserCommand.email());
        user.setFullName(createUserCommand.fullName());
        user.setPhoneNumber(createUserCommand.phoneNumber());
        user.setOrganization(createUserCommand.organization());
        user.setUsername(createUserCommand.username());
        user.setPassword(hashedPassword);
        user.setIdentificationNumber(createUserCommand.identificationNumber());
        user.setRole(defaultRole);
        user.setIsActive(true);

        // Guardar usuario
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
    public void setUserActiveStatus(Long idUser, boolean isActive) throws NotFoundException {
        UserEntity user = userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado "));

        user.setIsActive(isActive);
        userRepository.save(user);
    }


    @Override
    public UserEntity updateUser(Long idUser, UpdateUserRequest updateUserCommand) throws NotFoundException, DuplicatedEntityException {
        UserEntity existingUser = userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + idUser));

        // Validar datos
        validateUniqueFields(
                updateUserCommand.getUsername(),
                updateUserCommand.getEmail(),
                updateUserCommand.getIdentificationNumber(),
                idUser
        );


        existingUser.setEmail(updateUserCommand.getEmail());
        existingUser.setFullName(updateUserCommand.getFullName());
        existingUser.setPhoneNumber(updateUserCommand.getPhoneNumber());
        existingUser.setOrganization(updateUserCommand.getOrganization());
        existingUser.setUsername(updateUserCommand.getUsername());
        existingUser.setIdentificationNumber(updateUserCommand.getIdentificationNumber());

        return userRepository.save(existingUser);
    }

    @Override
    public void changeUserPassword(Long idUser, String newPassword) throws NotFoundException {
        UserEntity user = userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    private void validateUniqueFields(
            String username,
            String email,
            String identificationNumber,
            Long currentUserId // null si es create
    ) throws DuplicatedEntityException {
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
