package com.alessandro.congress_management.services.institution;

import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.institution.InstitutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstitutionServiceImplTest {
    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    // -------------- TEST GET ALL INSTITUTIONS --------------
    @Test
    void testGetAllInstitutions_shouldReturnAllInstitutions() {
        // Arrange
        InstitutionEntity institution1 = createInstitutionEntity(1L, "Institution 1", true);
        InstitutionEntity institution2 = createInstitutionEntity(2L, "Institution 2", false);
        List<InstitutionEntity> institutions = List.of(institution1, institution2);

        when(institutionRepository.findAll()).thenReturn(institutions);

        // Act
        List<InstitutionEntity> result = institutionService.getAllInstitutions();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Institution 1", result.get(0).getInstitutionName()),
                () -> assertEquals("Institution 2", result.get(1).getInstitutionName())
        );
    }

    @Test
    void testGetActiveInstitutions_shouldReturnOnlyActiveInstitutions() {
        // Arrange
        InstitutionEntity institution1 = createInstitutionEntity(1L, "Institution 1", true);
        InstitutionEntity institution2 = createInstitutionEntity(2L, "Institution 2", false);
        List<InstitutionEntity> activeInstitutions = List.of(institution1);

        when(institutionRepository.findByIsActiveTrue())
                .thenReturn(Optional.of(List.of(institution1)));

        // Act
        List<InstitutionEntity> result = institutionService.getActiveInstitutions();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertTrue(result.stream().allMatch(InstitutionEntity::getIsActive))
        );

    }

    // -------------- TEST GET INSTITUTION BY ID --------------
    @Test
    void testGetInstitutionById_shouldReturnInstitutionWhenFound() throws NotFoundException {
        // Arrange
        InstitutionEntity institution = createInstitutionEntity(1L, "Institution 1", true);
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));

        // Act
        InstitutionEntity result = institutionService.getInstitutionById(1L);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.getIdInstitution()),
                () -> assertEquals("Institution 1", result.getInstitutionName()),
                () -> assertTrue(result.getIsActive())
        );
    }

    @Test
    void testGetInstitutionById_shouldThrowNotFoundExceptionWhenNotFound() {
        // Arrange
        when(institutionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> institutionService.getInstitutionById(1L));
        assertEquals("Institution not found", exception.getMessage());
    }

    // --------------- TEST ACTIVE INSTITUTIONS NOT FOUND ---------------
    @Test
    void testGetActiveInstitutions_success() {
        // Arrange
        List<InstitutionEntity> activeInstitutions = Arrays.asList(
                createInstitutionEntity(1L, "USAC", true),
                createInstitutionEntity(2L, "UMG", true)
        );

        when(institutionRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeInstitutions));

        // Act
        List<InstitutionEntity> result = institutionService.getActiveInstitutions();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(InstitutionEntity::getIsActive),
                        "Todas deben estar activas"),
                () -> assertEquals("USAC", result.get(0).getInstitutionName()),
                () -> assertEquals("UMG", result.get(1).getInstitutionName())
        );

        verify(institutionRepository).findByIsActiveTrue();
    }

    @Test
    void testGetActiveInstitutions_shouldOnlyReturnActiveOnes() {
        // Arrange
        List<InstitutionEntity> activeInstitutions = Arrays.asList(
                createInstitutionEntity(1L, "Active1", true),
                createInstitutionEntity(2L, "Active2", true),
                createInstitutionEntity(3L, "Active3", true)
        );

        when(institutionRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeInstitutions));

        // Act
        List<InstitutionEntity> result = institutionService.getActiveInstitutions();

        // Assert - TODAS deben estar activas
        assertTrue(
                result.stream().allMatch(institution -> institution.getIsActive()),
                "Todas las instituciones retornadas deben estar activas"
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
        verify(institutionRepository).findByIsActiveTrue();
    }

    @Test
    void testGetActiveInstitutions_shouldDelegateToRepository() {
        // Arrange
        when(institutionRepository.findByIsActiveTrue())
                .thenReturn(Optional.of(Arrays.asList()));

        // Act
        institutionService.getActiveInstitutions();

        // Assert - Verificar que DELEGA al repository
        verify(institutionRepository, times(1)).findByIsActiveTrue();
    }

    private InstitutionEntity createInstitutionEntity(Long id, String name, boolean isActive) {;
        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(id);
        institution.setInstitutionName(name);
        institution.setIsActive(isActive);
        return institution;
    }


}
