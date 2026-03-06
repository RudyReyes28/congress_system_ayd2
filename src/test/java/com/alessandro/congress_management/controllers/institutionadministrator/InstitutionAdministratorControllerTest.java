package com.alessandro.congress_management.controllers.institutionadministrator;


import com.alessandro.congress_management.dto.institution_administrator.InstitutionNameRequest;
import com.alessandro.congress_management.dto.institution_administrator.UserInstitutionResponse;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.institution.InstitutionService;
import com.alessandro.congress_management.services.institution_administrator.InstitutionAdministratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstitutionAdministratorController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InstitutionAdministratorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstitutionAdministratorService institutionAdministratorService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testGetAdministratorsByInstitution() throws Exception {
        //Arrange
        InstitutionEntity institution = createInstitution(1L, "Test University");
        UserEntity user1 = createUser(1L, "admin1", "ADMIN_CONGRESS");
        UserEntity user2 = createUser(2L, "admin2", "ADMIN_CONGRESS");
        InstitutionAdministratorEntity admin1 = createInstitutionAdmin(1L, institution, user1);
        InstitutionAdministratorEntity admin2 = createInstitutionAdmin(2L, institution, user2);

        List<UserInstitutionResponse> expectedResponse = List.of(
            UserInstitutionResponse.fromEntity(admin1),
            UserInstitutionResponse.fromEntity(admin2)
        );

        when(institutionAdministratorService.getAdministratorsByInstitution(any(InstitutionNameRequest.class)))
            .thenReturn(expectedResponse);

        //Act & Assert
        mockMvc.perform(post("/api/v1/institution/administrators")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new InstitutionNameRequest("Test University"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].idUser").value(1))
            .andExpect(jsonPath("$[0].username").value("admin1"))
            .andExpect(jsonPath("$[1].idUser").value(2))
            .andExpect(jsonPath("$[1].username").value("admin2"));

        verify(institutionAdministratorService).getAdministratorsByInstitution(any(InstitutionNameRequest.class));


    }

    private InstitutionEntity createInstitution(Long id, String name) {
        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(id);
        institution.setInstitutionName(name);
        institution.setDescription("Test institution");
        institution.setContactEmail("contact@" + name.toLowerCase() + ".edu");
        institution.setContactPhone("555-0000");
        institution.setAddress("Test Address");
        institution.setIsActive(true);
        return institution;
    }

    private UserEntity createUser(Long id, String username, String roleName) {
        UserEntity user = new UserEntity();
        user.setIdUser(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("$2a$10$hashedPassword");
        user.setFullName("Test User " + username);
        user.setIdentificationNumber("ID" + id);
        user.setPhoneNumber("555-" + id);
        user.setOrganization("Test Org");
        user.setIsActive(true);

        RoleEntity role = new RoleEntity();
        role.setIdRole(1);
        role.setRoleName(roleName);
        role.setDescription("Test role");
        user.setRole(role);

        return user;
    }

    private InstitutionAdministratorEntity createInstitutionAdmin(Long id, InstitutionEntity institution, UserEntity user) {
        InstitutionAdministratorEntity admin = new InstitutionAdministratorEntity();
        admin.setIdInstitutionAdmin(id);
        admin.setInstitution(institution);
        admin.setUser(user);
        return admin;
    }


}
