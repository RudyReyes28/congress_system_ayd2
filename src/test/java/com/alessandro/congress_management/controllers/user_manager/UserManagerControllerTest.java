package com.alessandro.congress_management.controllers.user_manager;


import com.alessandro.congress_management.dto.user_manager.*;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.services.user_manager.UserManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserManagerController.class)
class UserManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagerService userManagerService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse mockUserResponse;
    private UserCongressAdminResponse mockCongressAdminResponse;

    @BeforeEach
    void setUp() {
        // Usamos el constructor all-args que Lombok @Value genera automáticamente}
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setIdRole(1);
        roleEntity.setRoleName("ADMIN_SYSTEM");

        UserEntity userEntity = new UserEntity();
        userEntity.setIdUser(1L);
        userEntity.setUsername("jdoe");
        userEntity.setEmail("jdoe@email.com");
        userEntity.setFullName("John Doe");
        userEntity.setPhoneNumber("555-1234");
        userEntity.setOrganization("Acme Corp");
        userEntity.setIdentificationNumber("ID-001");
        userEntity.setIsActive(true);
        userEntity.setRole(roleEntity);

        mockUserResponse = new UserResponse(userEntity );

        mockCongressAdminResponse = new UserCongressAdminResponse(
                "admin@email.com", "Admin Name", "555-9999",
                "Gov Org", "adminuser", "ID-999", "National Congress"
        );
    }

}