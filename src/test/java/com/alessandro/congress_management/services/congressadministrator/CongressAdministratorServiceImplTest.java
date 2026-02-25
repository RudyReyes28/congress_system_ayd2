package com.alessandro.congress_management.services.congressadministrator;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congressadministrator.UserCongressResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressAdministratorEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CongressAdministratorServiceImplTest {

    @Mock
    private CongressAdministratorRepository congressAdminRepository;

    @Mock
    private UserService userService;

    @Mock
    private CongressRepository congressRepository;

    @InjectMocks
    private CongressAdministratorServiceImpl congressAdminService;

    // ------------------- ASSIGN ADMINISTRATOR TO CONGRESS TESTS --------------

    @Test
    void testAssignAdministratorToCongress_success() throws Exception {
        // Arrange
        Long userId = 1L;
        Long congressId = 2L;
        UserEntity user = createUser(userId, "admin_user");
        CongressEntity congress = createCongress(congressId, "Tech Congress");

        when(userService.getUserById(userId)).thenReturn(user);
        when(congressAdminRepository.existsByUserAndCongress(user, congress)).thenReturn(false);
        when(congressAdminRepository.save(any(CongressAdministratorEntity.class)))
                .thenAnswer(invocation -> {
                    CongressAdministratorEntity entity = invocation.getArgument(0);
                    entity.setIdCongressAdmin(1L);
                    return entity;
                });

        ArgumentCaptor<CongressAdministratorEntity> captor =
                ArgumentCaptor.forClass(CongressAdministratorEntity.class);

        // Act
        CongressAdministratorEntity result =
                congressAdminService.assignAdministratorToCongress(userId, congress);

        // Assert
        verify(congressAdminRepository).save(captor.capture());
        CongressAdministratorEntity captured = captor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(user, captured.getUser()),
                () -> assertEquals(congress, captured.getCongress()),
                () -> assertNotNull(captured.getCreatedAt())
        );
    }

    @Test
    void testAssignAdministratorToCongress_whenUserNotFound_shouldThrowException() throws NotFoundException {
        // Arrange
        Long userId = 999L;
        Long congressId = 1L;

        when(userService.getUserById(userId))
                .thenThrow(new NotFoundException("Usuario no encontrado"));

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> congressAdminService.assignAdministratorToCongress(userId, createCongress(congressId, "Tech Congress"))
        );

        verify(congressRepository, never()).findById(any());
        verify(congressAdminRepository, never()).save(any());
    }



    @Test
    void testAssignAdministratorToCongress_whenAlreadyAdmin_shouldThrowException() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        Long congressId = 2L;
        UserEntity user = createUser(userId, "admin_user");
        CongressEntity congress = createCongress(congressId, "Tech Congress");

        when(userService.getUserById(userId)).thenReturn(user);
        when(congressAdminRepository.existsByUserAndCongress(user, congress)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressAdminService.assignAdministratorToCongress(userId, congress)
        );

        assertTrue(exception.getMessage().contains("is already an admin"));
        assertTrue(exception.getMessage().contains(userId.toString()));
        assertTrue(exception.getMessage().contains(congressId.toString()));
        verify(congressAdminRepository, never()).save(any());
    }


    // ------------------ GET CONGRESSES BY ADMINISTRATOR TESTS ---------------

    @Test
    void testGetCongressesByAdministrator_success() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        List<CongressAdministratorEntity> congressAdmins = Arrays.asList(
                createCongressAdmin(1L, userId, 1L, "Congress 1"),
                createCongressAdmin(2L, userId, 2L, "Congress 2"),
                createCongressAdmin(3L, userId, 3L, "Congress 3")
        );

        when(congressAdminRepository.findByUser_IdUser(userId)).thenReturn(congressAdmins);

        // Act
        List<CongressResponse> result = congressAdminService.getCongressesByAdministrator(userId);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size()),
                () -> assertEquals("Congress 1", result.get(0).getCongressName()),
                () -> assertEquals("Congress 2", result.get(1).getCongressName()),
                () -> assertEquals("Congress 3", result.get(2).getCongressName())
        );

        verify(congressAdminRepository).findByUser_IdUser(userId);
    }

    @Test
    void testGetCongressesByAdministrator_whenNoCongressesFound_shouldThrowException() {
        // Arrange
        Long userId = 999L;

        when(congressAdminRepository.findByUser_IdUser(userId)).thenReturn(Arrays.asList());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> congressAdminService.getCongressesByAdministrator(userId)
        );

        assertTrue(exception.getMessage().contains("No congresses found"));
        assertTrue(exception.getMessage().contains(userId.toString()));
    }

    //------------------- GET ADMINISTRATORS BY CONGRESS TESTS ---------------
     @Test
    void testGetAdministratorsByCongress_success() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        List<CongressAdministratorEntity> congressAdmins = Arrays.asList(
                createCongressAdmin(1L, 1L, congressId, "Congress 1"),
                createCongressAdmin(2L, 2L, congressId, "Congress 1"),
                createCongressAdmin(3L, 3L, congressId, "Congress 1")
        );

        when(congressAdminRepository.findByCongress_IdCongress(congressId)).thenReturn(congressAdmins);

        // Act
        List<UserCongressResponse> result = congressAdminService.getAdministratorsByCongress(congressId);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size()),
                () -> assertEquals("user1", result.get(0).getUsername()),
                () -> assertEquals("user2", result.get(1).getUsername()),
                () -> assertEquals("user3", result.get(2).getUsername())
        );

        verify(congressAdminRepository).findByCongress_IdCongress(congressId);
     }

    @Test
    void testGetAdministratorsByCongress_whenNoAdminsFound_shouldThrowException() {
        // Arrange
        Long congressId = 999L;

        when(congressAdminRepository.findByCongress_IdCongress(congressId)).thenReturn(Arrays.asList());

        //Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> congressAdminService.getAdministratorsByCongress(congressId)
        );

        assertTrue(exception.getMessage().contains("No administrators found"));
        assertTrue(exception.getMessage().contains(congressId.toString()));
    }

    //------------------- REMOVE ADMINISTRATOR FROM CONGRESS TESTS ---------------

    @Test
    void testRemoveAdministratorFromCongress_success() throws NotFoundException, BusinessRuleException {
        // Arrange
        Long userId = 1L;
        Long congressId = 1L;
        UserEntity user = createUser(userId, "admin_user");
        CongressEntity congress = createCongress(congressId, "Tech Congress");
        CongressAdministratorEntity congressAdmin = createCongressAdmin(1L, userId, congressId, "Tech Congress");

        when(userService.getUserById(userId)).thenReturn(user);
        when(congressAdminRepository.countByCongress_IdCongress(congressId)).thenReturn(2);
        when(congressAdminRepository.findByUser_IdUserAndCongress_IdCongress(userId, congressId))
                .thenReturn(java.util.Optional.of(congressAdmin));

        // Act
        congressAdminService.removeAdministratorFromCongress(userId, congress);

        // Assert
        verify(congressAdminRepository).delete(congressAdmin);
    }

    @Test
    void testRemoveAdministratorFromCongress_whenTryingToRemoveLastAdmin_shouldThrowException() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        Long congressId = 1L;
        UserEntity user = createUser(userId, "admin_user");
        CongressEntity congress = createCongress(congressId, "Tech Congress");

        when(userService.getUserById(userId)).thenReturn(user);
        when(congressAdminRepository.countByCongress_IdCongress(congressId)).thenReturn(1);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressAdminService.removeAdministratorFromCongress(userId, congress)
        );
        assertTrue(exception.getMessage().contains("Cannot remove the last administrator"));

        verify(congressAdminRepository, never()).delete(any());
    }

    @Test
    void testRemoveAdministratorFromCongress_whenAdminNotFound_shouldThrowException() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        Long congressId = 1L;
        UserEntity user = createUser(userId, "admin_user");
        CongressEntity congress = createCongress(congressId, "Tech Congress");

        when(userService.getUserById(userId)).thenReturn(user);
        when(congressAdminRepository.countByCongress_IdCongress(congressId)).thenReturn(2);
        when(congressAdminRepository.findByUser_IdUserAndCongress_IdCongress(userId, congressId))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> congressAdminService.removeAdministratorFromCongress(userId, congress)
        );
        assertTrue(exception.getMessage().contains("User  is not an admin of congress"));

        verify(congressAdminRepository, never()).delete(any());
    }


    // ----------- HELPER METHODS ------------------

    private UserEntity createUser(Long id, String username) {
        UserEntity user = new UserEntity();
        user.setIdUser(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFullName("Test User " + username);
        user.setIsActive(true);
        return user;
    }

    private CongressEntity createCongress(Long id, String name) {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(id);
        congress.setCongressName(name);
        congress.setDescription("Description for " + name);
        congress.setStartDate(LocalDate.of(2026, 5, 15));
        congress.setEndDate(LocalDate.of(2026, 5, 17));
        congress.setLocation("Guatemala City");
        congress.setPrice(new BigDecimal("150.00"));
        congress.setIsActive(true);

        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(1L);
        institution.setInstitutionName("USAC");
        congress.setInstitution(institution);

        return congress;
    }

    private CongressAdministratorEntity createCongressAdmin(
            Long adminId, Long userId, Long congressId, String congressName) {

        CongressAdministratorEntity congressAdmin = new CongressAdministratorEntity();
        congressAdmin.setIdCongressAdmin(adminId);

        UserEntity user = createUser(userId, "user" + userId);
        congressAdmin.setUser(user);

        CongressEntity congress = createCongress(congressId, congressName);
        congressAdmin.setCongress(congress);

        return congressAdmin;
    }
}