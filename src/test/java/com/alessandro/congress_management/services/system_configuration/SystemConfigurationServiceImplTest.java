package com.alessandro.congress_management.services.system_configuration;

import com.alessandro.congress_management.dto.system_configuration.SystemConfigurationResponse;
import com.alessandro.congress_management.dto.system_configuration.UpdateConfigValue;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import com.alessandro.congress_management.repositories.system_configuration.SystemConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigurationServiceImplTest {

    @Mock
    private SystemConfigurationRepository systemConfigurationRepository;

    @InjectMocks
    private SystemConfigurationServiceImpl systemConfigurationService;

    // -------------------- GET ALL CONFIGURATIONS TESTS -----------------------

    @Test
    void testGetAllConfigurations_success() {
        // Arrange
        List<SystemConfigurationEntity> configs = Arrays.asList(
                createConfig(1, "COMMISSION_PERCENTAGE", "10", "Percentage of commission for each congress"),
                createConfig(2, "MIN_CONGRESS_PRICE", "35", "Minimum price for congresses")
        );

        when(systemConfigurationRepository.findAll()).thenReturn(configs);

        // Act
        List<SystemConfigurationEntity> result = systemConfigurationService.getAllConfigurations();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals("COMMISSION_PERCENTAGE", result.get(0).getConfigKey()),
                () -> assertEquals("MIN_CONGRESS_PRICE", result.get(1).getConfigKey())
        );

        verify(systemConfigurationRepository).findAll();
    }

    @Test
    void testGetAllConfigurations_whenEmpty_shouldReturnEmptyList() {
        // Arrange
        when(systemConfigurationRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<SystemConfigurationEntity> result = systemConfigurationService.getAllConfigurations();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(systemConfigurationRepository).findAll();
    }


    // ---------------- GET CONFIGURATION BY KEY TESTS ---------------------

    @Test
    void testGetConfigurationByKey_success() throws NotFoundException {
        // Arrange
        String configKey = "COMMISSION_PERCENTAGE";
        SystemConfigurationEntity config = createConfig(1, configKey, "10", "Commission percentage for each congress");

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.of(config));

        // Act
        SystemConfigurationEntity result = systemConfigurationService.getConfigurationByKey(configKey);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(configKey, result.getConfigKey()),
                () -> assertEquals("10", result.getConfigValue()),
                () -> assertEquals("Commission percentage for each congress", result.getDescription())
        );

        verify(systemConfigurationRepository).findByConfigKey(configKey);
    }

    @Test
    void testGetConfigurationByKey_whenNotFound_shouldThrowException() {
        // Arrange
        String configKey = "NON_EXISTENT_KEY";

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> systemConfigurationService.getConfigurationByKey(configKey)
        );

        assertEquals("Configuration with key NON_EXISTENT_KEY not found", exception.getMessage());
        verify(systemConfigurationRepository).findByConfigKey(configKey);
    }



    // ---------------------- UPDATE CONFIGURATION VALUE TESTS ---------------------

    @Test
    void testUpdateConfigurationConfigValue_success() throws BusinessRuleException, NotFoundException {
        // Arrange
        String configKey = "COMMISSION_PERCENTAGE";
        UpdateConfigValue updateValue = new UpdateConfigValue("50");
        SystemConfigurationEntity existingConfig = createConfig(1, configKey, "10", "Commission percentage for each congress");
        LocalDateTime beforeUpdate = existingConfig.getUpdatedAt();

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.of(existingConfig));
        when(systemConfigurationRepository.save(any(SystemConfigurationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<SystemConfigurationEntity> captor = ArgumentCaptor.forClass(SystemConfigurationEntity.class);

        // Act
        SystemConfigurationResponse result = systemConfigurationService.updateConfigurationConfigValue(configKey, updateValue);

        // Assert
        verify(systemConfigurationRepository).save(captor.capture());
        SystemConfigurationEntity captured = captor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(configKey, result.getConfigKey()),
                () -> assertEquals("50", result.getConfigValue()),
                () -> assertEquals("50", captured.getConfigValue()),
                () -> assertNotNull(captured.getUpdatedAt()),
                () -> assertTrue(captured.getUpdatedAt().isAfter(beforeUpdate) ||
                        captured.getUpdatedAt().isEqual(beforeUpdate))
        );
    }

    @Test
    void testUpdateConfigurationConfigValue_whenConfigNotFound_shouldThrowException() {
        // Arrange
        String configKey = "NON_EXISTENT_KEY";
        UpdateConfigValue updateValue = new UpdateConfigValue("50");

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> systemConfigurationService.updateConfigurationConfigValue(configKey, updateValue)
        );

        assertEquals("Configuration with key NON_EXISTENT_KEY not found", exception.getMessage());
        verify(systemConfigurationRepository, never()).save(any());
    }
/*
    @Test
    void testUpdateConfigurationConfigValue_shouldUpdateTimestamp()
            throws BusinessRuleException, NotFoundException {
        // Arrange
        String configKey = "MAX_FILE_SIZE";
        UpdateConfigValue updateValue = new UpdateConfigValue("50");
        SystemConfigurationEntity config = createConfig(1, configKey, "10", "Description");
        LocalDateTime oldTimestamp = config.getUpdatedAt();

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.of(config));
        when(systemConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<SystemConfigurationEntity> captor = ArgumentCaptor.forClass(SystemConfigurationEntity.class);

        // Act
        systemConfigurationService.updateConfigurationConfigValue(configKey, updateValue);

        // Assert
        verify(systemConfigurationRepository).save(captor.capture());
        SystemConfigurationEntity captured = captor.getValue();

        assertNotNull(captured.getUpdatedAt());
        assertTrue(captured.getUpdatedAt().isAfter(oldTimestamp) ||
                captured.getUpdatedAt().isEqual(LocalDateTime.now()));
    }

    @Test
    void testUpdateConfigurationConfigValue_withZeroValue_success()
            throws BusinessRuleException, NotFoundException {
        // Arrange
        String configKey = "MIN_VALUE";
        UpdateConfigValue updateValue = new UpdateConfigValue("0");
        SystemConfigurationEntity config = createConfig(1, configKey, "10", "Description");

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.of(config));
        when(systemConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<SystemConfigurationEntity> captor = ArgumentCaptor.forClass(SystemConfigurationEntity.class);

        // Act
        SystemConfigurationResponse result = systemConfigurationService.updateConfigurationConfigValue(configKey, updateValue);

        // Assert
        verify(systemConfigurationRepository).save(captor.capture());
        assertEquals("0", captor.getValue().getConfigValue());
        assertEquals("0", result.getConfigValue());
    }

    @Test
    void testUpdateConfigurationConfigValue_withMaxValue_success()
            throws BusinessRuleException, NotFoundException {
        // Arrange
        String configKey = "MAX_VALUE";
        UpdateConfigValue updateValue = new UpdateConfigValue("100");
        SystemConfigurationEntity config = createConfig(1, configKey, "50", "Description");

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.of(config));
        when(systemConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<SystemConfigurationEntity> captor = ArgumentCaptor.forClass(SystemConfigurationEntity.class);

        // Act
        SystemConfigurationResponse result = systemConfigurationService.updateConfigurationConfigValue(configKey, updateValue);

        // Assert
        verify(systemConfigurationRepository).save(captor.capture());
        assertEquals("100", captor.getValue().getConfigValue());
        assertEquals("100", result.getConfigValue());
    }

    @Test
    void testUpdateConfigurationConfigValue_shouldNotChangeOtherFields()
            throws BusinessRuleException, NotFoundException {
        // Arrange
        String configKey = "TEST_KEY";
        UpdateConfigValue updateValue = new UpdateConfigValue("75");
        SystemConfigurationEntity config = createConfig(1, configKey, "25", "Original Description");

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.of(config));
        when(systemConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<SystemConfigurationEntity> captor = ArgumentCaptor.forClass(SystemConfigurationEntity.class);

        // Act
        systemConfigurationService.updateConfigurationConfigValue(configKey, updateValue);

        // Assert
        verify(systemConfigurationRepository).save(captor.capture());
        SystemConfigurationEntity captured = captor.getValue();

        assertAll(
                () -> assertEquals(1, captured.getIdConfig(), "ID no debe cambiar"),
                () -> assertEquals(configKey, captured.getConfigKey(), "ConfigKey no debe cambiar"),
                () -> assertEquals("Original Description", captured.getDescription(), "Description no debe cambiar"),
                () -> assertEquals("75", captured.getConfigValue(), "ConfigValue debe actualizarse")
        );
    }

    @Test
    void testUpdateConfigurationConfigValue_multipleUpdates_shouldUpdateEachTime()
            throws BusinessRuleException, NotFoundException {
        // Arrange
        String configKey = "DYNAMIC_CONFIG";
        SystemConfigurationEntity config = createConfig(1, configKey, "10", "Description");

        UpdateConfigValue update1 = new UpdateConfigValue("20");
        UpdateConfigValue update2 = new UpdateConfigValue("30");
        UpdateConfigValue update3 = new UpdateConfigValue("40");

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.of(config));
        when(systemConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SystemConfigurationResponse result1 = systemConfigurationService.updateConfigurationConfigValue(configKey, update1);
        SystemConfigurationResponse result2 = systemConfigurationService.updateConfigurationConfigValue(configKey, update2);
        SystemConfigurationResponse result3 = systemConfigurationService.updateConfigurationConfigValue(configKey, update3);

        // Assert
        assertEquals("20", result1.getConfigValue());
        assertEquals("30", result2.getConfigValue());
        assertEquals("40", result3.getConfigValue());
        verify(systemConfigurationRepository, times(3)).save(any());
    }

    @Test
    void testUpdateConfigurationConfigValue_shouldReturnCorrectResponse()
            throws BusinessRuleException, NotFoundException {
        // Arrange
        String configKey = "TEST_CONFIG";
        UpdateConfigValue updateValue = new UpdateConfigValue("85");
        SystemConfigurationEntity config = createConfig(5, configKey, "50", "Test Description");

        when(systemConfigurationRepository.findByConfigKey(configKey)).thenReturn(Optional.of(config));
        when(systemConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SystemConfigurationResponse result = systemConfigurationService.updateConfigurationConfigValue(configKey, updateValue);

        // Assert
        assertAll(
                () -> assertEquals(5, result.getIdConfig()),
                () -> assertEquals(configKey, result.getConfigKey()),
                () -> assertEquals("85", result.getConfigValue()),
                () -> assertEquals("Test Description", result.getDescription()),
                () -> assertNotNull(result.getUpdatedAt())
        );
    }
*/
    // -------------------------- HELPER METHODS ---------------------

    private SystemConfigurationEntity createConfig(Integer id, String key, String value, String description) {
        SystemConfigurationEntity config = new SystemConfigurationEntity();
        config.setIdConfig(id);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(description);
        config.setUpdatedAt(LocalDateTime.now().minusDays(1)); // Timestamp antiguo para tests
        return config;
    }
}