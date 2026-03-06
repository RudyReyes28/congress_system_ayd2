package com.alessandro.congress_management.services.user;

import com.alessandro.congress_management.dto.user_manager.CreateUserCommand;
import com.alessandro.congress_management.dto.user_manager.UpdateUserRequest;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;


import java.util.List;

public interface UserService {
    //create user
    UserEntity createUser(CreateUserCommand createUserCommand) throws DuplicatedEntityException;

    //get user by id
    UserEntity getUserById(Long idUser) throws NotFoundException;

    //List all users
    List<UserEntity> getAllUsers() ;

    List<UserEntity> findActiveUsers();

    //Activate and deactivate user
    void setUserActiveStatus(Long idUser, boolean isActive) throws NotFoundException;

    //Update user
    UserEntity updateUser(Long idUser, UpdateUserRequest updateUserRequest) throws NotFoundException, DuplicatedEntityException;


    //Change user password
    void changeUserPassword(Long idUser, String newPassword) throws NotFoundException;


}
