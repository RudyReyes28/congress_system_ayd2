package com.alessandro.congress_management.controllers.system_configuration;

import com.alessandro.congress_management.dto.system_configuration.SystemConfigurationResponse;
import com.alessandro.congress_management.dto.system_configuration.UpdateConfigValue;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.system_configuration.SystemConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
class SystemConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SystemConfigurationService systemConfigurationService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // =----------------------- GET ALL CONFIGURATIONS TESTS ------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetAllConfigurations_success() throws Exception {
        // Arrange
        List<SystemConfigurationEntity> configs = Arrays.asList(
                createConfig(1, "COMMISSION_PERCENTAGE", "10", "Commission percentage for ticket sales"),
                createConfig(2, "MIN_CONGRESS_PRICE", "35", "Minimum price for congresses")
        );

        when(systemConfigurationService.getAllConfigurations()).thenReturn(configs);

        // Act & Assert
        mockMvc.perform(get("/api/v1/system-configurations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].configKey").value("COMMISSION_PERCENTAGE"))
                .andExpect(jsonPath("$[0].configValue").value("10"))
                .andExpect(jsonPath("$[1].configKey").value("MIN_CONGRESS_PRICE"))
                .andExpect(jsonPath("$[1].configValue").value("35"));

        verify(systemConfigurationService).getAllConfigurations();
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetAllConfigurations_whenEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(systemConfigurationService.getAllConfigurations()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/system-configurations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(systemConfigurationService).getAllConfigurations();
    }


    // ----------------------- GET CONFIGURATION BY KEY TESTS --------------------

    /*@Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetConfigurationByKey_success() throws Exception {
        // Arrange
        String configKey = "MAX_FILE_SIZE";
        SystemConfigurationEntity config = createConfig(1, configKey, "10", "Maximum file size");

        when(systemConfigurationService.getConfigurationByKey(configKey)).thenReturn(config);

        // Act & Assert
        mockMvc.perform(get("/api/v1/system-configurations/{configKey}", configKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configKey").value(configKey))
                .andExpect(jsonPath("$.configValue").value("10"))
                .andExpect(jsonPath("$.description").value("Maximum file size"));

        verify(systemConfigurationService).getConfigurationByKey(configKey);
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetConfigurationByKey_whenNotFound_shouldReturn404() throws Exception {
        // Arrange
        String configKey = "NON_EXISTENT_KEY";

        when(systemConfigurationService.getConfigurationByKey(configKey))
                .thenThrow(new NotFoundException("Configuration with key " + configKey + " not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/system-configurations/{configKey}", configKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(systemConfigurationService).getConfigurationByKey(configKey);
    }*/



    // -------------------- UPDATE CONFIGURATION VALUE TESTS -------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateConfigurationValue_success() throws Exception {
        // Arrange
        String configKey = "COMMISSION_PERCENTAGE";
        UpdateConfigValue request = new UpdateConfigValue("50");
        SystemConfigurationResponse response = createConfigResponse(1, configKey, "50", "Commission percentage for congress");

        when(systemConfigurationService.updateConfigurationConfigValue(eq(configKey), any(UpdateConfigValue.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/system-configurations/{configKey}", configKey)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configKey").value(configKey))
                .andExpect(jsonPath("$.configValue").value("50"))
                .andExpect(jsonPath("$.description").value("Commission percentage for congress"));

        verify(systemConfigurationService).updateConfigurationConfigValue(eq(configKey), any(UpdateConfigValue.class));
    }


    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateConfigurationValue_whenConfigNotFound_shouldReturn404() throws Exception {
        // Arrange
        String configKey = "NON_EXISTENT_KEY";
        UpdateConfigValue request = new UpdateConfigValue("50");

        when(systemConfigurationService.updateConfigurationConfigValue(eq(configKey), any(UpdateConfigValue.class)))
                .thenThrow(new NotFoundException("Configuration with key " + configKey + " not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/system-configurations/{configKey}", configKey)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(systemConfigurationService).updateConfigurationConfigValue(eq(configKey), any(UpdateConfigValue.class));
    }



    // -------------- HELPER METHODS ------------------

    private SystemConfigurationEntity createConfig(Integer id, String key, String value, String description) {
        SystemConfigurationEntity config = new SystemConfigurationEntity();
        config.setIdConfig(id);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(description);
        config.setUpdatedAt(LocalDateTime.now());
        return config;
    }

    private SystemConfigurationResponse createConfigResponse(Integer id, String key, String value, String description) {
        return new SystemConfigurationResponse(
                id,
                key,
                value,
                description,
                LocalDateTime.now()
        );
    }
}