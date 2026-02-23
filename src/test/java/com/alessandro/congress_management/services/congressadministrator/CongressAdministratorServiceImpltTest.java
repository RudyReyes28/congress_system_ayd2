package com.alessandro.congress_management.services.congressadministrator;

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
import java.util.Optional;

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
        when(congressRepository.findById(congressId)).thenReturn(Optional.of(congress));
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
                congressAdminService.assignAdministratorToCongress(userId, congressId);

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
                () -> congressAdminService.assignAdministratorToCongress(userId, congressId)
        );

        verify(congressRepository, never()).findById(any());
        verify(congressAdminRepository, never()).save(any());
    }

    @Test
    void testAssignAdministratorToCongress_whenCongressNotFound_shouldThrowException() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        Long congressId = 999L;
        UserEntity user = createUser(userId, "admin_user");

        when(userService.getUserById(userId)).thenReturn(user);
        when(congressRepository.findById(congressId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> congressAdminService.assignAdministratorToCongress(userId, congressId)
        );

        assertTrue(exception.getMessage().contains("Congress not found"));
        assertTrue(exception.getMessage().contains("999"));
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
        when(congressRepository.findById(congressId)).thenReturn(Optional.of(congress));
        when(congressAdminRepository.existsByUserAndCongress(user, congress)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressAdminService.assignAdministratorToCongress(userId, congressId)
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
        List<CongressEntity> result = congressAdminService.getCongressesByAdministrator(userId);

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