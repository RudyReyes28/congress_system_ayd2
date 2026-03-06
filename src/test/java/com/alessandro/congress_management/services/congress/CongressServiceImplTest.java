package com.alessandro.congress_management.services.congress;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congress.CreateCongressRequest;
import com.alessandro.congress_management.dto.congress.UpdateCongressRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressAdministratorEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.services.congressadministrator.CongressAdministratorService;
import com.alessandro.congress_management.services.institution_administrator.InstitutionAdministratorService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CongressServiceImplTest {

    @Mock
    private CongressRepository congressRepository;

    @Mock
    private InstitutionAdministratorService institutionAdminService;

    @Mock
    private CongressAdministratorService congressAdminService;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private CongressServiceImpl congressService;

    // --------------- CREATE CONGRESS TESTS ----------------

    @Test
    void testCreateCongress_success() throws Exception {
        // Arrange
        CreateCongressRequest request = createCongressRequest();
        InstitutionAdministratorEntity institutionAdmin = createInstitutionAdmin(1L, 1L, true);
        CongressEntity savedCongress = createCongress(1L, "Tech Congress 2026", true);

        when(congressRepository.existsByCongressName("Tech Congress 2026")).thenReturn(false);
        when(institutionAdminService.findInstitutionAdministratorByIdAdministrator(1L))
                .thenReturn(institutionAdmin);
        when(congressRepository.save(any(CongressEntity.class))).thenReturn(savedCongress);
        when(congressAdminService.assignAdministratorToCongress(1L, savedCongress))
                .thenReturn(new CongressAdministratorEntity());

        ArgumentCaptor<CongressEntity> captor = ArgumentCaptor.forClass(CongressEntity.class);

        // Act
        CongressResponse result = congressService.createCongress(request);

        // Assert
        verify(congressRepository).save(captor.capture());
        CongressEntity captured = captor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Tech Congress 2026", result.getCongressName()),
                () -> assertEquals("USAC", result.getInstitutionName()),
                () -> assertTrue(result.isActive()),
                () -> assertNotNull(captured.getInstitution())
        );

        verify(congressAdminService).assignAdministratorToCongress(1L, savedCongress);
    }

    @Test
    void testCreateCongress_whenNameExists_shouldThrowException() {
        // Arrange
        CreateCongressRequest request = createCongressRequest();

        when(congressRepository.existsByCongressName("Tech Congress 2026")).thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> congressService.createCongress(request)
        );

        assertEquals("A congress with the same name already exists", exception.getMessage());
        verify(congressRepository, never()).save(any());
    }

    @Test
    void testCreateCongress_whenStartDateAfterEndDate_shouldThrowException() {
        // Arrange
        CreateCongressRequest request = new CreateCongressRequest(
                1L,
                "Tech Congress 2026",
                "Description",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 5, 1), // End date antes de start date
                "Guatemala City",
                new BigDecimal("150.00")
        );

        when(congressRepository.existsByCongressName(anyString())).thenReturn(false);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressService.createCongress(request)
        );

        assertEquals("Start date cannot be after end date", exception.getMessage());
        verify(congressRepository, never()).save(any());
    }

    @Test
    void testCreateCongress_whenInstitutionInactive_shouldThrowException() throws NotFoundException {
        // Arrange
        CreateCongressRequest request = createCongressRequest();
        InstitutionAdministratorEntity institutionAdmin = createInstitutionAdmin(1L, 1L, false);

        when(congressRepository.existsByCongressName(anyString())).thenReturn(false);
        when(institutionAdminService.findInstitutionAdministratorByIdAdministrator(1L))
                .thenReturn(institutionAdmin);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressService.createCongress(request)
        );

        assertEquals("The institution of the congress manager must be active", exception.getMessage());
        verify(congressRepository, never()).save(any());
    }

    @Test
    void testCreateCongress_whenInstitutionAdminNotFound_shouldThrowException() throws NotFoundException {
        // Arrange
        CreateCongressRequest request = createCongressRequest();

        when(congressRepository.existsByCongressName(anyString())).thenReturn(false);
        when(institutionAdminService.findInstitutionAdministratorByIdAdministrator(1L))
                .thenThrow(new NotFoundException("Institution administrator not found"));

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> congressService.createCongress(request)
        );

        verify(congressRepository, never()).save(any());
    }

    @Test
    void testCreateCongress_shouldSetCorrectInstitution() throws Exception {
        // Arrange
        CreateCongressRequest request = createCongressRequest();
        InstitutionAdministratorEntity institutionAdmin = createInstitutionAdmin(1L, 5L, true);

        when(congressRepository.existsByCongressName(anyString())).thenReturn(false);
        when(institutionAdminService.findInstitutionAdministratorByIdAdministrator(1L))
                .thenReturn(institutionAdmin);
        when(congressRepository.save(any())).thenAnswer(inv -> {
            CongressEntity entity = inv.getArgument(0);
            entity.setIdCongress(1L);
            return entity;
        });
        when(congressAdminService.assignAdministratorToCongress(anyLong(),  any()))
                .thenReturn(new CongressAdministratorEntity());

        ArgumentCaptor<CongressEntity> captor = ArgumentCaptor.forClass(CongressEntity.class);

        // Act
        congressService.createCongress(request);

        // Assert
        verify(congressRepository).save(captor.capture());
        CongressEntity captured = captor.getValue();

        assertEquals(5L, captured.getInstitution().getIdInstitution());
    }

    // ------------ UPDATE CONGRESS TESTS ---------------

    @Test
    void testUpdateCongress_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        UpdateCongressRequest request = createUpdateRequest();
        CongressEntity existingCongress = createCongress(congressId, "Old Name", true);

        when(congressRepository.existsByCongressNameAndIdCongressNot("Updated Congress", congressId))
                .thenReturn(false);
        when(congressRepository.findById(congressId)).thenReturn(Optional.of(existingCongress));
        when(registrationRepository.existsByCongress_IdCongress(congressId)).thenReturn(false);
        when(congressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<CongressEntity> captor = ArgumentCaptor.forClass(CongressEntity.class);

        // Act
        CongressResponse result = congressService.updateCongress(congressId, request);

        // Assert
        verify(congressRepository).save(captor.capture());
        CongressEntity captured = captor.getValue();

        assertAll(
                () -> assertEquals("Updated Congress", captured.getCongressName()),
                () -> assertEquals("Updated description", captured.getDescription()),
                () -> assertEquals("New Location", captured.getLocation())
        );
    }

    @Test
    void testUpdateCongress_whenStartDateAfterEndDate_shouldThrowException() {
        // Arrange
        Long congressId = 1L;
        UpdateCongressRequest request = new UpdateCongressRequest(
                "Congress",
                "Description",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 5, 1), // End date antes
                "Location",
                new BigDecimal("150.00")
        );

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressService.updateCongress(congressId, request)
        );

        assertEquals("Start date cannot be after end date", exception.getMessage());
        verify(congressRepository, never()).save(any());
    }

    @Test
    void testUpdateCongress_whenNameExists_shouldThrowException() {
        // Arrange
        Long congressId = 1L;
        UpdateCongressRequest request = createUpdateRequest();

        when(congressRepository.existsByCongressNameAndIdCongressNot("Updated Congress", congressId))
                .thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressService.updateCongress(congressId, request)
        );

        assertEquals("Another congress with the same name already exists", exception.getMessage());
        verify(congressRepository, never()).save(any());
    }

    @Test
    void testUpdateCongress_whenCongressNotFound_shouldThrowException() {
        // Arrange
        Long congressId = 999L;
        UpdateCongressRequest request = createUpdateRequest();

        when(congressRepository.existsByCongressNameAndIdCongressNot(anyString(), eq(congressId)))
                .thenReturn(false);
        when(congressRepository.findById(congressId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> congressService.updateCongress(congressId, request)
        );

        verify(congressRepository, never()).save(any());
    }

    @Test
    void testUpdateCongress_whenPriceChangedWithRegistrations_shouldThrowException() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        UpdateCongressRequest request = createUpdateRequest(); // Price: 200.00
        CongressEntity existingCongress = createCongress(congressId, "Congress", true);
        existingCongress.setPrice(new BigDecimal("150.00")); // Precio diferente

        when(congressRepository.existsByCongressNameAndIdCongressNot(anyString(), eq(congressId)))
                .thenReturn(false);
        when(congressRepository.findById(congressId)).thenReturn(Optional.of(existingCongress));
        when(registrationRepository.existsByCongress_IdCongress(congressId)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressService.updateCongress(congressId, request)
        );

        assertEquals("Cannot change the price of the congress because there are already registrations",
                exception.getMessage());
        verify(congressRepository, never()).save(any());
    }

    //--------------------- ADD/REMOVE ADMIN TESTS -----------------
    @Test
    void testAddAdministrator_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Congress", true);

        when(congressRepository.findById(congressId)).thenReturn(Optional.of(congress));
        when(institutionAdminService.isUserAdminOfInstitution(userId, congress.getInstitution().getIdInstitution()))
                .thenReturn(true);
        when(congressAdminService.assignAdministratorToCongress(userId, congress))
                .thenReturn(new CongressAdministratorEntity());

        // Act
        congressService.addAdministrator(congressId, userId);

        // Assert
        verify(congressAdminService).assignAdministratorToCongress(userId, congress);
    }

    @Test
    void testAddAdministrator_whenCongressNotActive_shouldThrowException() throws NotFoundException, BusinessRuleException {
        // Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Congress", false);

        when(congressRepository.findById(congressId)).thenReturn(Optional.of(congress));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressService.addAdministrator(congressId, userId)
        );

        assertEquals("Congress is not active", exception.getMessage());
        verify(congressAdminService, never()).assignAdministratorToCongress(anyLong(), any());
    }

    @Test
    void testAddAdministrator_whenUserNotAdminOfInstitution_shouldThrowException() throws NotFoundException, BusinessRuleException {
        // Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Congress", true);

        when(congressRepository.findById(congressId)).thenReturn(Optional.of(congress));
        when(institutionAdminService.isUserAdminOfInstitution(userId, congress.getInstitution().getIdInstitution()))
                .thenReturn(false);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> congressService.addAdministrator(congressId, userId)
        );

        assertEquals("User does not belong to the institution", exception.getMessage());
        verify(congressAdminService, never()).assignAdministratorToCongress(anyLong(), any());
    }

    @Test
    void testRemoveAdministrator_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Congress", true);

        when(congressRepository.findById(congressId)).thenReturn(Optional.of(congress));

        // Act
        congressService.removeAdministrator(congressId, userId);

        // Assert
        verify(congressAdminService).removeAdministratorFromCongress(userId, congress);
    }

    // ---------------------- FIND CONGRESS BY ID TESTS --------------

    @Test
    void testFindCongressEntityById_success() throws NotFoundException {
        // Arrange
        Long congressId = 1L;
        CongressEntity congress = createCongress(congressId, "Tech Congress", true);

        when(congressRepository.findById(congressId)).thenReturn(Optional.of(congress));

        // Act
        CongressEntity result = congressService.findCongressEntityById(congressId);

        // Assert
        assertNotNull(result);
        assertEquals(congressId, result.getIdCongress());
        assertEquals("Tech Congress", result.getCongressName());
    }

    @Test
    void testFindCongressEntityById_whenNotFound_shouldThrowException() {
        // Arrange
        Long congressId = 999L;

        when(congressRepository.findById(congressId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> congressService.findCongressEntityById(congressId)
        );

        assertTrue(exception.getMessage().contains("Congress not found"));
        assertTrue(exception.getMessage().contains("999"));
    }

    // ------------ GET ALL CONGRESSES TESTS -----------------

    @Test
    void testGetAllCongresses_success() {
        // Arrange
        List<CongressEntity> congresses = Arrays.asList(
                createCongress(1L, "Congress 1", true),
                createCongress(2L, "Congress 2", false),
                createCongress(3L, "Congress 3", true)
        );

        when(congressRepository.findAll()).thenReturn(congresses);

        // Act
        List<CongressResponse> result = congressService.getAllCongresses();

        // Assert
        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("Congress 1", result.get(0).getCongressName()),
                () -> assertEquals("Congress 2", result.get(1).getCongressName()),
                () -> assertEquals("Congress 3", result.get(2).getCongressName())
        );
    }

    @Test
    void testGetAllCongresses_whenEmpty_shouldReturnEmptyList() {
        // Arrange
        when(congressRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<CongressResponse> result = congressService.getAllCongresses();

        // Assert
        assertTrue(result.isEmpty());
    }

    // -------------- GET ACTIVE CONGRESSES TESTS -----------------

    @Test
    void testGetActiveCongresses_success() {
        // Arrange
        List<CongressEntity> activeCongresses = Arrays.asList(
                createCongress(1L, "Active 1", true),
                createCongress(2L, "Active 2", true)
        );

        when(congressRepository.findByIsActiveTrue()).thenReturn(activeCongresses);

        // Act
        List<CongressResponse> result = congressService.getActiveCongresses();

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(CongressResponse::isActive))
        );
    }

    @Test
    void testGetActiveCongresses_whenEmpty_shouldReturnEmptyList() {
        // Arrange
        when(congressRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());

        // Act
        List<CongressResponse> result = congressService.getActiveCongresses();

        // Assert
        assertTrue(result.isEmpty());
    }



    // ---------------- HELPER METHODS -------------------

    private CreateCongressRequest createCongressRequest() {
        return new CreateCongressRequest(
                1L,
                "Tech Congress 2026",
                "A technology conference",
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 17),
                "Guatemala City",
                new BigDecimal("150.00")
        );
    }

    private UpdateCongressRequest createUpdateRequest() {
        return new UpdateCongressRequest(
                "Updated Congress",
                "Updated description",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5),
                "New Location",
                new BigDecimal("200.00")
        );
    }

    private CongressEntity createCongress(Long id, String name, boolean isActive) {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(id);
        congress.setCongressName(name);
        congress.setDescription("Description for " + name);
        congress.setStartDate(LocalDate.of(2026, 5, 15));
        congress.setEndDate(LocalDate.of(2026, 5, 17));
        congress.setLocation("Guatemala City");
        congress.setPrice(new BigDecimal("150.00"));
        congress.setIsActive(isActive);

        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(1L);
        institution.setInstitutionName("USAC");
        institution.setIsActive(true);
        congress.setInstitution(institution);

        return congress;
    }

    private InstitutionAdministratorEntity createInstitutionAdmin(Long adminId, Long institutionId, boolean institutionActive) {
        InstitutionAdministratorEntity institutionAdmin = new InstitutionAdministratorEntity();
        institutionAdmin.setIdInstitutionAdmin(adminId);

        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(institutionId);
        institution.setInstitutionName("USAC");
        institution.setIsActive(institutionActive);
        institutionAdmin.setInstitution(institution);

        UserEntity user = new UserEntity();
        user.setIdUser(1L);
        user.setUsername("admin_user");
        institutionAdmin.setUser(user);

        return institutionAdmin;
    }
}