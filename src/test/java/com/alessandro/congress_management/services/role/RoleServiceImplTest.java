package com.alessandro.congress_management.services.role;

import com.alessandro.congress_management.dto.role.RoleResponse;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.repositories.authenticate.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    private static final String ROLE_ADMIN_SYSTEM = "ADMIN_SYSTEM";
    private static final String ROLE_ADMIN_CONGRESS = "ADMIN_CONGRESS";

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    //------------- TEST GET ALL ROLES -------------
    @Test
    void testGetAllRoles_shouldReturnAllRoles() {
        // Arrange
        RoleEntity role1 = createRoleEntity(1, ROLE_ADMIN_SYSTEM);
        RoleEntity role2 = createRoleEntity(2, ROLE_ADMIN_CONGRESS);

        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));

        // Act
        List<RoleResponse> roles = roleService.getAllRoles();

        // Assert
        assertAll(
                () -> assertNotNull(roles),
                () -> assertEquals(2, roles.size()),
                () -> assertEquals(ROLE_ADMIN_SYSTEM, roles.get(0).getNameRole()),
                () -> assertEquals(ROLE_ADMIN_CONGRESS, roles.get(1).getNameRole())
        );
    }

    @Test
    void testGetAllRoles_shouldReturnEmptyListWhenNoRoles() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(List.of());

        // Act
        List<RoleResponse> roles = roleService.getAllRoles();

        // Assert
        assertNotNull(roles);
        assertTrue(roles.isEmpty());

        verify(roleRepository).findAll();
    }



    private RoleEntity createRoleEntity(Integer id, String roleName) {
        RoleEntity role = new RoleEntity();
        role.setIdRole(id);
        role.setRoleName(roleName);
        return role;
    }
}
