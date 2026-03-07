package com.alessandro.congress_management.services.user;

import com.alessandro.congress_management.dto.user_manager.CreateUserCommand;
import com.alessandro.congress_management.dto.user_manager.UpdateUserRequest;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;


import java.util.List;

public interface UserService {
    UserEntity createUser(CreateUserCommand createUserCommand) throws DuplicatedEntityException;

    UserEntity createInactiveUser(CreateUserCommand command) throws DuplicatedEntityException;

    UserEntity getUserById(Long idUser) throws NotFoundException;

    List<UserEntity> getAllUsers() ;

    List<UserEntity> findActiveUsers();

    void setUserActiveStatus(Long idUser, boolean isActive) throws NotFoundException;

    UserEntity updateUser(Long idUser, UpdateUserRequest updateUserRequest) throws NotFoundException, DuplicatedEntityException;

    void changeUserPassword(Long idUser, String newPassword) throws NotFoundException;


}
