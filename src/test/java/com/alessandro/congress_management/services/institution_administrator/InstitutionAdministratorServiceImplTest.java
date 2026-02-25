package com.alessandro.congress_management.services.institution_administrator;
import com.alessandro.congress_management.dto.institution_administrator.CreateInstitutionAdministratorRequest;
import com.alessandro.congress_management.dto.institution_administrator.InstitutionNameRequest;
import com.alessandro.congress_management.dto.institution_administrator.UserInstitutionResponse;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.congress_management.InstitutionAdministratorRepository;
import com.alessandro.congress_management.services.institution.InstitutionService;
import com.alessandro.congress_management.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionAdministratorServiceImplTest {

    @Mock
    private InstitutionAdministratorRepository institutionAdministratorRepository;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private InstitutionAdministratorServiceImpl institutionAdministratorService;

    // --------------------- TEST CREATE INSTITUTION ADMINISTRATOR -------------------

    @Test
    void testCreateInstitutionAdministrator_success() throws NotFoundException {
        // Arrange
        Long institutionId = 1L;
        Long userId = 2L;
        CreateInstitutionAdministratorRequest request = new CreateInstitutionAdministratorRequest(
                institutionId,
                userId
        );

        InstitutionEntity institution = createInstitution(institutionId, "USAC");
        UserEntity user = createUser(userId, "admin_congress", "ADMIN_CONGRESS");

        when(institutionService.getInstitutionById(institutionId)).thenReturn(institution);
        when(userService.getUserById(userId)).thenReturn(user);
        when(institutionAdministratorRepository.save(any(InstitutionAdministratorEntity.class)))
                .thenAnswer(invocation -> {
                    InstitutionAdministratorEntity entity = invocation.getArgument(0);
                    entity.setIdInstitutionAdmin(1L);
                    return entity;
                });

        ArgumentCaptor<InstitutionAdministratorEntity> captor =
                ArgumentCaptor.forClass(InstitutionAdministratorEntity.class);

        // Act
        InstitutionAdministratorEntity result =
                institutionAdministratorService.createInstitutionAdministrator(request);

        // Assert
        verify(institutionAdministratorRepository).save(captor.capture());
        InstitutionAdministratorEntity captured = captor.getValue();

        assertAll(
                () -> assertNotNull(result, "Result no debe ser null"),
                () -> assertEquals(institution, captured.getInstitution(),
                        "Institución debe coincidir"),
                () -> assertEquals(user, captured.getUser(),
                        "Usuario debe coincidir"),
                () -> assertNotNull(captured.getCreatedAt(),
                        "CreatedAt debe estar establecido")
        );
    }

    @Test
    void testCreateInstitutionAdministrator_shouldValidateInstitutionExists() throws NotFoundException {
        // Arrange
        Long institutionId = 1L;
        Long userId = 2L;
        CreateInstitutionAdministratorRequest request = new CreateInstitutionAdministratorRequest(
                institutionId,
                userId
        );

        InstitutionEntity institution = createInstitution(institutionId, "USAC");
        UserEntity user = createUser(userId, "admin", "ADMIN_CONGRESS");

        when(institutionService.getInstitutionById(institutionId)).thenReturn(institution);
        when(userService.getUserById(userId)).thenReturn(user);
        when(institutionAdministratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        institutionAdministratorService.createInstitutionAdministrator(request);

        // Assert - Verificar que se valida la existencia de la institución
        verify(institutionService).getInstitutionById(institutionId);
    }

    @Test
    void testCreateInstitutionAdministrator_shouldValidateUserExists() throws NotFoundException {
        // Arrange
        Long institutionId = 1L;
        Long userId = 2L;
        CreateInstitutionAdministratorRequest request = new CreateInstitutionAdministratorRequest(
                institutionId,
                userId
        );

        InstitutionEntity institution = createInstitution(institutionId, "USAC");
        UserEntity user = createUser(userId, "admin", "ADMIN_CONGRESS");

        when(institutionService.getInstitutionById(institutionId)).thenReturn(institution);
        when(userService.getUserById(userId)).thenReturn(user);
        when(institutionAdministratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        institutionAdministratorService.createInstitutionAdministrator(request);

        // Assert - Verificar que se valida la existencia del usuario
        verify(userService).getUserById(userId);
    }

    @Test
    void testCreateInstitutionAdministrator_whenInstitutionNotFound_shouldThrowException()
            throws NotFoundException {
        // Arrange
        Long institutionId = 999L;
        Long userId = 2L;
        CreateInstitutionAdministratorRequest request = new CreateInstitutionAdministratorRequest(
                institutionId,
                userId
        );

        when(institutionService.getInstitutionById(institutionId))
                .thenThrow(new NotFoundException("Institution not found"));

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> institutionAdministratorService.createInstitutionAdministrator(request)
        );

        assertEquals("Institution not found", exception.getMessage());
        verify(institutionService).getInstitutionById(institutionId);
        verify(userService, never()).getUserById(anyLong());
        verify(institutionAdministratorRepository, never()).save(any());
    }

    @Test
    void testCreateInstitutionAdministrator_whenUserNotFound_shouldThrowException()
            throws NotFoundException {
        // Arrange
        Long institutionId = 1L;
        Long userId = 999L;
        CreateInstitutionAdministratorRequest request = new CreateInstitutionAdministratorRequest(
                institutionId,
                userId
        );

        InstitutionEntity institution = createInstitution(institutionId, "USAC");

        when(institutionService.getInstitutionById(institutionId)).thenReturn(institution);
        when(userService.getUserById(userId))
                .thenThrow(new NotFoundException("Usuario no encontrado"));

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> institutionAdministratorService.createInstitutionAdministrator(request)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(institutionService).getInstitutionById(institutionId);
        verify(userService).getUserById(userId);
        verify(institutionAdministratorRepository, never()).save(any());
    }

    @Test
    void testCreateInstitutionAdministrator_shouldSaveWithCorrectRelations() throws NotFoundException {
        // Arrange
        Long institutionId = 1L;
        Long userId = 2L;
        CreateInstitutionAdministratorRequest request = new CreateInstitutionAdministratorRequest(
                institutionId,
                userId
        );

        InstitutionEntity institution = createInstitution(institutionId, "Universidad USAC");
        UserEntity user = createUser(userId, "admin_congress", "ADMIN_CONGRESS");

        when(institutionService.getInstitutionById(institutionId)).thenReturn(institution);
        when(userService.getUserById(userId)).thenReturn(user);
        when(institutionAdministratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<InstitutionAdministratorEntity> captor =
                ArgumentCaptor.forClass(InstitutionAdministratorEntity.class);

        // Act
        institutionAdministratorService.createInstitutionAdministrator(request);

        // Assert - Verificar que las relaciones se establecen correctamente
        verify(institutionAdministratorRepository).save(captor.capture());
        InstitutionAdministratorEntity captured = captor.getValue();

        assertAll(
                () -> assertNotNull(captured.getInstitution(), "Institución no debe ser null"),
                () -> assertNotNull(captured.getUser(), "Usuario no debe ser null"),
                () -> assertEquals(institutionId, captured.getInstitution().getIdInstitution(),
                        "ID de institución debe coincidir"),
                () -> assertEquals(userId, captured.getUser().getIdUser(),
                        "ID de usuario debe coincidir"),
                () -> assertEquals("Universidad USAC", captured.getInstitution().getInstitutionName(),
                        "Nombre de institución debe coincidir"),
                () -> assertEquals("admin_congress", captured.getUser().getUsername(),
                        "Username debe coincidir")
        );
    }

    @Test
    void testCreateInstitutionAdministrator_shouldDelegateToServices() throws NotFoundException {
        // Arrange
        Long institutionId = 1L;
        Long userId = 2L;
        CreateInstitutionAdministratorRequest request = new CreateInstitutionAdministratorRequest(
                institutionId,
                userId
        );

        InstitutionEntity institution = createInstitution(institutionId, "USAC");
        UserEntity user = createUser(userId, "admin", "ADMIN_CONGRESS");

        when(institutionService.getInstitutionById(institutionId)).thenReturn(institution);
        when(userService.getUserById(userId)).thenReturn(user);
        when(institutionAdministratorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        institutionAdministratorService.createInstitutionAdministrator(request);

        // Assert - Verificar delegación a servicios
        verify(institutionService, times(1)).getInstitutionById(institutionId);
        verify(userService, times(1)).getUserById(userId);
        verify(institutionAdministratorRepository, times(1)).save(any());
    }

    @Test
    void testCreateInstitutionAdministrator_multipleAdminsForSameInstitution_shouldSucceed()
            throws NotFoundException {
        // Arrange
        Long institutionId = 1L;
        InstitutionEntity institution = createInstitution(institutionId, "USAC");

        // Primer admin
        CreateInstitutionAdministratorRequest request1 = new CreateInstitutionAdministratorRequest(
                institutionId, 1L
        );
        UserEntity user1 = createUser(1L, "admin1", "ADMIN_CONGRESS");

        // Segundo admin
        CreateInstitutionAdministratorRequest request2 = new CreateInstitutionAdministratorRequest(
                institutionId, 2L
        );
        UserEntity user2 = createUser(2L, "admin2", "ADMIN_CONGRESS");

        when(institutionService.getInstitutionById(institutionId)).thenReturn(institution);
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(user2);
        when(institutionAdministratorRepository.save(any())).thenAnswer(inv -> {
            InstitutionAdministratorEntity entity = inv.getArgument(0);
            entity.setIdInstitutionAdmin(System.currentTimeMillis());
            return entity;
        });

        // Act
        InstitutionAdministratorEntity result1 =
                institutionAdministratorService.createInstitutionAdministrator(request1);
        InstitutionAdministratorEntity result2 =
                institutionAdministratorService.createInstitutionAdministrator(request2);

        // Assert - Ambos admins deben poder ser asignados a la misma institución
        assertAll(
                () -> assertNotNull(result1),
                () -> assertNotNull(result2),
                () -> assertEquals(institutionId, result1.getInstitution().getIdInstitution()),
                () -> assertEquals(institutionId, result2.getInstitution().getIdInstitution()),
                () -> assertNotEquals(result1.getUser().getIdUser(), result2.getUser().getIdUser())
        );

        verify(institutionAdministratorRepository, times(2)).save(any());
    }

    //----------------------------- TEST FIND INSTITUTION ADMINISTRATOR BY ID -------------------
    @Test
    void testFindInstitutionAdministratorByIdAdministrator_success() throws NotFoundException {
        // Arrange
        Long adminId = 1L;
        InstitutionAdministratorEntity institutionAdmin = new InstitutionAdministratorEntity();
        institutionAdmin.setIdInstitutionAdmin(1L);
        institutionAdmin.setUser(createUser(adminId, "admin_congress", "ADMIN_CONGRESS"));
        institutionAdmin.setInstitution(createInstitution(1L, "USAC"));

        when(institutionAdministratorRepository.findByUser_IdUser(adminId))
                .thenReturn(Optional.of(institutionAdmin));

        // Act
        InstitutionAdministratorEntity result =
                institutionAdministratorService.findInstitutionAdministratorByIdAdministrator(adminId);

        // Assert
        assertAll(
                () -> assertNotNull(result, "Result no debe ser null"),
                () -> assertEquals(institutionAdmin.getIdInstitutionAdmin(), result.getIdInstitutionAdmin(),
                        "ID de institución admin debe coincidir"),
                () -> assertEquals(institutionAdmin.getUser().getIdUser(), result.getUser().getIdUser(),
                        "ID de usuario debe coincidir"),
                () -> assertEquals(institutionAdmin.getInstitution().getIdInstitution(),
                        result.getInstitution().getIdInstitution(),
                        "ID de institución debe coincidir")
        );
    }

    @Test
    void testFindInstitutionAdministratorByIdAdministrator_whenNotFound_shouldThrowException() {
        // Arrange
        Long adminId = 999L;

        when(institutionAdministratorRepository.findByUser_IdUser(adminId))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> institutionAdministratorService.findInstitutionAdministratorByIdAdministrator(adminId)
        );
        assertEquals("Institution administrator not found with id: " + adminId, exception.getMessage());
        verify(institutionAdministratorRepository).findByUser_IdUser(adminId);
    }

    // --------------------- TEST IS USER ADMIN OF INSTITUTION -------------------
    @Test
    void testIsUserAdminOfInstitution_success() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        Long institutionId = 2L;

        when(institutionAdministratorRepository.existsByUser_IdUserAndInstitution_IdInstitution(userId, institutionId))
                .thenReturn(true);

        // Act
        boolean result = institutionAdministratorService.isUserAdminOfInstitution(userId, institutionId);

        // Assert
        assertTrue(result, "El usuario debería ser admin de la institución");
        verify(institutionAdministratorRepository).existsByUser_IdUserAndInstitution_IdInstitution(userId, institutionId);
    }

    @Test
    void testIsUserAdminOfInstitution_whenNotAdmin_shouldReturnFalse() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        Long institutionId = 2L;

        when(institutionAdministratorRepository.existsByUser_IdUserAndInstitution_IdInstitution(userId, institutionId))
                .thenReturn(false);

        // Act
        boolean result = institutionAdministratorService.isUserAdminOfInstitution(userId, institutionId);

        // Assert
        assertFalse(result, "El usuario no debería ser admin de la institución");
        verify(institutionAdministratorRepository).existsByUser_IdUserAndInstitution_IdInstitution(userId, institutionId);
    }

    // ---------------------- TEST GET ADMINISTRATOR BY INSTITUTION -------------------
    @Test
    void testGetAdministratorsByInstitution_success() throws NotFoundException {
        // Arrange
        String institutionName = "USAC";
        InstitutionEntity institution = createInstitution(1L, institutionName);
        InstitutionAdministratorEntity institutionAdmin = createInstitutionAdmin(1L, institution, createUser(1L, "admin_congress", "ADMIN_CONGRESS"));

        when(institutionAdministratorRepository.findByInstitution_InstitutionName(institutionName))
                .thenReturn(List.of(institutionAdmin));

        // Act
        List<UserInstitutionResponse> result = institutionAdministratorService.getAdministratorsByInstitution(new InstitutionNameRequest(institutionName));

        // Assert
        assertAll(
                () -> assertNotNull(result, "Result no debe ser null"),
                () -> assertEquals(1, result.size(), "Debe haber un administrador en la lista"),
                () -> assertEquals(institutionAdmin.getUser().getIdUser(), result.get(0).getIdUser(),
                        "ID de usuario debe coincidir"),
                () -> assertEquals(institutionAdmin.getUser().getUsername(), result.get(0).getUsername(),
                        "Username debe coincidir"),
                () -> assertEquals(institutionAdmin.getInstitution().getInstitutionName(),
                        result.get(0).getInstitutionName(),
                        "Nombre de institución debe coincidir")
        );
    }


    // --------------------- METODOS AUXILIARES -------------------

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