package com.alessandro.congress_management.controllers.role;


import com.alessandro.congress_management.dto.role.RoleResponse;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.role.RoleService;
import com.alessandro.congress_management.services.room.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    //------------- TEST GET ALL ROLES -------------
    @Test
    void testGetAllRoles_shouldReturnAllRoles() throws Exception {
        //Arrange
        RoleEntity role1 = createRoleEntity(1, "ADMIN_SYSTEM");
        RoleEntity role2 = createRoleEntity(2, "ADMIN_CONGRESS");

        RoleResponse roleResponse1 = new RoleResponse(role1);
        RoleResponse roleResponse2 = new RoleResponse(role2);
        List<RoleResponse> expectedRoles = List.of(roleResponse1, roleResponse2);

        when(roleService.getAllRoles()).thenReturn(expectedRoles);

        //Act & Assert
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedRoles)))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nameRole").value("ADMIN_SYSTEM"))
                .andExpect(jsonPath("$[1].nameRole").value("ADMIN_CONGRESS"));

        verify(roleService).getAllRoles();

    }


    private RoleEntity createRoleEntity(Integer id, String roleName) {
        RoleEntity role = new RoleEntity();
        role.setIdRole(id);
        role.setRoleName(roleName);
        return role;
    }
}
