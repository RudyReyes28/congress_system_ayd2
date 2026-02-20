package com.alessandro.congress_management.services.institution;

import com.alessandro.congress_management.dto.institution.CreateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.InstitutionResponse;
import com.alessandro.congress_management.dto.institution.UpdateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.UpdateStatusInstitutionRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.institution.InstitutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private CongressRepository congressRepository;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    // ------------------ GET ALL INSTITUTIONS TESTS ------------------------

    @Test
    void testGetAllInstitutions_success() {
        // Arrange
        List<InstitutionEntity> institutions = Arrays.asList(
                createInstitution(1L, "USAC", true),
                createInstitution(2L, "UMG", true),
                createInstitution(3L, "URL", false)
        );

        when(institutionRepository.findAll()).thenReturn(institutions);

        // Act
        List<InstitutionEntity> result = institutionService.getAllInstitutions();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size()),
                () -> assertEquals("USAC", result.get(0).getInstitutionName()),
                () -> assertEquals("UMG", result.get(1).getInstitutionName()),
                () -> assertEquals("URL", result.get(2).getInstitutionName())
        );

        verify(institutionRepository).findAll();
    }

    @Test
    void testGetAllInstitutions_whenEmpty_shouldReturnEmptyList() {
        // Arrange
        when(institutionRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<InstitutionEntity> result = institutionService.getAllInstitutions();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---------------------- GET ACTIVE INSTITUTIONS TESTS -----------------------

    @Test
    void testGetActiveInstitutions_success() {
        // Arrange
        List<InstitutionEntity> activeInstitutions = Arrays.asList(
                createInstitution(1L, "USAC", true),
                createInstitution(2L, "UMG", true)
        );

        when(institutionRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeInstitutions));

        // Act
        List<InstitutionEntity> result = institutionService.getActiveInstitutions();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(InstitutionEntity::getIsActive))
        );
    }

    @Test
    void testGetActiveInstitutions_whenNoActiveInstitutions_shouldThrowException() {
        // Arrange
        when(institutionRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> institutionService.getActiveInstitutions()
        );

        assertEquals("No active institutions found", exception.getMessage());
    }

    // ------------------ GET INSTITUTION BY ID TESTS-------------------

    @Test
    void testGetInstitutionById_success() throws NotFoundException {
        // Arrange
        Long institutionId = 1L;
        InstitutionEntity institution = createInstitution(institutionId, "USAC", true);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));

        // Act
        InstitutionEntity result = institutionService.getInstitutionById(institutionId);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(institutionId, result.getIdInstitution()),
                () -> assertEquals("USAC", result.getInstitutionName())
        );
    }

    @Test
    void testGetInstitutionById_whenNotFound_shouldThrowException() {
        // Arrange
        Long institutionId = 999L;

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> institutionService.getInstitutionById(institutionId)
        );

        assertEquals("Institution not found", exception.getMessage());
    }

    // ----------------------- CREATE INSTITUTION TESTS ------------------------

    @Test
    void testCreateInstitution_success() throws DuplicatedEntityException {
        // Arrange
        CreateInstitutionRequest request = createInstitutionRequest();
        InstitutionEntity savedInstitution = createInstitution(1L, "USAC", true);

        when(institutionRepository.existsByInstitutionName("USAC")).thenReturn(false);
        when(institutionRepository.existsByContactEmail("contact@usac.edu")).thenReturn(false);
        when(institutionRepository.save(any(InstitutionEntity.class))).thenReturn(savedInstitution);

        ArgumentCaptor<InstitutionEntity> captor = ArgumentCaptor.forClass(InstitutionEntity.class);

        // Act
        InstitutionResponse result = institutionService.createInstitution(request);

        // Assert
        verify(institutionRepository).save(captor.capture());
        InstitutionEntity captured = captor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.getIdInstitution()),
                () -> assertEquals("USAC", result.getInstitutionName()),
                () -> assertEquals("Description for USAC", result.getDescription()),
                () -> assertEquals("usac@example.com", result.getContactEmail()),
                () -> assertTrue(result.isActive()),
                () -> assertEquals("USAC", captured.getInstitutionName()),
                () -> assertTrue(captured.getIsActive())
        );
    }

    @Test
    void testCreateInstitution_whenNameExists_shouldThrowException() {
        // Arrange
        CreateInstitutionRequest request = createInstitutionRequest();

        when(institutionRepository.existsByInstitutionName("USAC")).thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> institutionService.createInstitution(request)
        );

        assertEquals("Institution with the same name already exists", exception.getMessage());
        verify(institutionRepository, never()).save(any());
    }

    @Test
    void testCreateInstitution_whenEmailExists_shouldThrowException() {
        // Arrange
        CreateInstitutionRequest request = createInstitutionRequest();

        when(institutionRepository.existsByInstitutionName("USAC")).thenReturn(false);
        when(institutionRepository.existsByContactEmail("contact@usac.edu")).thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> institutionService.createInstitution(request)
        );

        assertEquals("Institution with the same contact email already exists", exception.getMessage());
        verify(institutionRepository, never()).save(any());
    }


    // --------------- UPDATE INSTITUTION TESTS -----------------

    @Test
    void testUpdateInstitution_success() throws NotFoundException, DuplicatedEntityException {
        // Arrange
        Long institutionId = 1L;
        UpdateInstitutionRequest request = createUpdateRequest();
        InstitutionEntity existingInstitution = createInstitution(institutionId, "USAC", true);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(existingInstitution));
        when(institutionRepository.existsByInstitutionNameAndIdInstitutionNot("USAC Updated", institutionId))
                .thenReturn(false);
        when(institutionRepository.save(any(InstitutionEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<InstitutionEntity> captor = ArgumentCaptor.forClass(InstitutionEntity.class);

        // Act
        InstitutionResponse result = institutionService.updateInstitution(institutionId, request);

        // Assert
        verify(institutionRepository).save(captor.capture());
        InstitutionEntity captured = captor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("USAC Updated", captured.getInstitutionName()),
                () -> assertEquals("Updated description", captured.getDescription()),
                () -> assertEquals("New address", captured.getAddress()),
                () -> assertEquals("new@usac.edu", captured.getContactEmail()),
                () -> assertEquals("555-9999", captured.getContactPhone())
        );
    }

    @Test
    void testUpdateInstitution_whenNotFound_shouldThrowException() {
        // Arrange
        Long institutionId = 999L;
        UpdateInstitutionRequest request = createUpdateRequest();

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> institutionService.updateInstitution(institutionId, request)
        );

        verify(institutionRepository, never()).save(any());
    }

    @Test
    void testUpdateInstitution_whenNameExists_shouldThrowException() {
        // Arrange
        Long institutionId = 1L;
        UpdateInstitutionRequest request = createUpdateRequest();
        InstitutionEntity existingInstitution = createInstitution(institutionId, "USAC", true);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(existingInstitution));
        when(institutionRepository.existsByInstitutionNameAndIdInstitutionNot("USAC Updated", institutionId))
                .thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> institutionService.updateInstitution(institutionId, request)
        );

        assertEquals("Institution with the same name already exists", exception.getMessage());
        verify(institutionRepository, never()).save(any());
    }

    @Test
    void testUpdateInstitution_shouldNotChangeActiveStatus() throws NotFoundException, DuplicatedEntityException {
        // Arrange
        Long institutionId = 1L;
        UpdateInstitutionRequest request = createUpdateRequest();
        InstitutionEntity existingInstitution = createInstitution(institutionId, "USAC", true);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(existingInstitution));
        when(institutionRepository.existsByInstitutionNameAndIdInstitutionNot(anyString(), eq(institutionId)))
                .thenReturn(false);
        when(institutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<InstitutionEntity> captor = ArgumentCaptor.forClass(InstitutionEntity.class);

        // Act
        institutionService.updateInstitution(institutionId, request);

        // Assert
        verify(institutionRepository).save(captor.capture());
        assertTrue(captor.getValue().getIsActive(), "Update no debe cambiar el estado activo");
    }

    // ------------------ UPDATE STATUS TESTS ---------------

    @Test
    void testUpdateStatusInstitution_deactivate_success() throws NotFoundException, BusinessRuleException {
        // Arrange
        Long institutionId = 1L;
        UpdateStatusInstitutionRequest statusUpdate = new UpdateStatusInstitutionRequest(false);
        InstitutionEntity institution = createInstitution(institutionId, "USAC", true);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(congressRepository.existsByIsActiveTrueAndInstitution_IdInstitution(institutionId)).thenReturn(false);
        when(institutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<InstitutionEntity> captor = ArgumentCaptor.forClass(InstitutionEntity.class);

        // Act
        institutionService.uptateStatusInstitution(institutionId, statusUpdate);

        // Assert
        verify(institutionRepository).save(captor.capture());
        assertFalse(captor.getValue().getIsActive());
    }

    @Test
    void testUpdateStatusInstitution_activate_success() throws NotFoundException, BusinessRuleException {
        // Arrange
        Long institutionId = 1L;
        UpdateStatusInstitutionRequest statusUpdate = new UpdateStatusInstitutionRequest(true);
        InstitutionEntity institution = createInstitution(institutionId, "USAC", false);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(institutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<InstitutionEntity> captor = ArgumentCaptor.forClass(InstitutionEntity.class);

        // Act
        institutionService.uptateStatusInstitution(institutionId, statusUpdate);

        // Assert
        verify(institutionRepository).save(captor.capture());
        assertTrue(captor.getValue().getIsActive());
    }

    @Test
    void testUpdateStatusInstitution_whenInstitutionNotFound_shouldThrowException() {
        // Arrange
        Long institutionId = 999L;
        UpdateStatusInstitutionRequest statusUpdate = new UpdateStatusInstitutionRequest(false);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> institutionService.uptateStatusInstitution(institutionId, statusUpdate)
        );

        verify(institutionRepository, never()).save(any());
    }

    @Test
    void testUpdateStatusInstitution_whenHasActiveCongresses_shouldThrowException() {
        // Arrange
        Long institutionId = 1L;
        UpdateStatusInstitutionRequest statusUpdate = new UpdateStatusInstitutionRequest(false);
        InstitutionEntity institution = createInstitution(institutionId, "USAC", true);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(congressRepository.existsByIsActiveTrueAndInstitution_IdInstitution(institutionId)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> institutionService.uptateStatusInstitution(institutionId, statusUpdate)
        );

        assertEquals("Cannot deactivate institution with active congresses", exception.getMessage());
        verify(institutionRepository, never()).save(any());
    }

    @Test
    void testUpdateStatusInstitution_activateDoesNotCheckCongresses() throws NotFoundException, BusinessRuleException {
        // Arrange
        Long institutionId = 1L;
        UpdateStatusInstitutionRequest statusUpdate = new UpdateStatusInstitutionRequest(true);
        InstitutionEntity institution = createInstitution(institutionId, "USAC", false);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(institutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        institutionService.uptateStatusInstitution(institutionId, statusUpdate);

        // Assert - NO debe verificar congresos al activar
        verify(congressRepository, never()).existsByIsActiveTrueAndInstitution_IdInstitution(anyLong());
    }

    @Test
    void testUpdateStatusInstitution_deactivateWithNoActiveCongresses_success()
            throws NotFoundException, BusinessRuleException {
        // Arrange
        Long institutionId = 1L;
        UpdateStatusInstitutionRequest statusUpdate = new UpdateStatusInstitutionRequest(false);
        InstitutionEntity institution = createInstitution(institutionId, "USAC", true);

        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(congressRepository.existsByIsActiveTrueAndInstitution_IdInstitution(institutionId)).thenReturn(false);
        when(institutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        institutionService.uptateStatusInstitution(institutionId, statusUpdate);

        // Assert
        verify(congressRepository).existsByIsActiveTrueAndInstitution_IdInstitution(institutionId);
        verify(institutionRepository).save(any());
    }

    // -------------------- HELPER METHODS ----------------------

    private InstitutionEntity createInstitution(Long id, String name, boolean isActive) {
        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(id);
        institution.setInstitutionName(name);
        institution.setDescription("Description for " + name);
        institution.setContactEmail(name.toLowerCase() + "@example.com");
        institution.setContactPhone("555-" + id);
        institution.setAddress("Address " + id);
        institution.setIsActive(isActive);
        return institution;
    }

    private CreateInstitutionRequest createInstitutionRequest() {
        return new CreateInstitutionRequest(
                "USAC",
                "Universidad de San Carlos",
                "Ciudad Universitaria Zona 12",
                "contact@usac.edu",
                "555-1234"
        );
    }

    private UpdateInstitutionRequest createUpdateRequest() {
        return new UpdateInstitutionRequest(
                "USAC Updated",
                "Updated description",
                "New address",
                "new@usac.edu",
                "555-9999"
        );
    }
}