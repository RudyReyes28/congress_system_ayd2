package com.alessandro.congress_management.services.user_manager;

import com.alessandro.congress_management.dto.user_manager.*;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;

import java.util.List;

public interface UserManagerService {


    //Crear usuario
    UserResponse createUserByAdmin(CreateUserRequest createUserRequest) throws DuplicatedEntityException;

    //Listar usuarios
    List<UserResponse> getAllUsers();

    //Editar estado de usuario
    void setUserActiveStatus(Long idUser, UpdateStatusUser updateStatusUser) throws DuplicatedEntityException, NotFoundException;

    //Actualizar usuario
    UserResponse updateUserByAdmin(Long idUser, UpdateUserRequest updateUserRequest) throws DuplicatedEntityException, NotFoundException;

    //Actualizar contraseña de usuario
    void changeUserPasswordByAdmin(Long idUser, UpdateUserPassword updatePassword) throws NotFoundException;


}
