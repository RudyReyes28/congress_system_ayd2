package com.alessandro.congress_management.controllers.institution;

import com.alessandro.congress_management.dto.institution.CreateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.InstitutionResponse;
import com.alessandro.congress_management.dto.institution.UpdateInstitutionRequest;
import com.alessandro.congress_management.dto.institution.UpdateStatusInstitutionRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.institution.InstitutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstitutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InstitutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstitutionService institutionService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // --------------- GET ALL INSTITUTIONS TESTS --------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetAllInstitutions_success() throws Exception {
        // Arrange
        List<InstitutionEntity> institutions = Arrays.asList(
                createInstitution(1L, "USAC", true),
                createInstitution(2L, "UMG", true),
                createInstitution(3L, "URL", false)
        );

        when(institutionService.getAllInstitutions()).thenReturn(institutions);

        // Act & Assert
        mockMvc.perform(get("/api/v1/institutions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].institutionName").value("USAC"))
                .andExpect(jsonPath("$[1].institutionName").value("UMG"))
                .andExpect(jsonPath("$[2].institutionName").value("URL"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[2].active").value(false));

        verify(institutionService).getAllInstitutions();
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetAllInstitutions_whenEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(institutionService.getAllInstitutions()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/institutions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    // ---------------------- GET ACTIVE INSTITUTIONS TESTS --------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetActiveInstitutions_success() throws Exception {
        // Arrange
        List<InstitutionEntity> activeInstitutions = Arrays.asList(
                createInstitution(1L, "USAC", true),
                createInstitution(2L, "UMG", true)
        );

        when(institutionService.getActiveInstitutions()).thenReturn(activeInstitutions);

        // Act & Assert
        mockMvc.perform(get("/api/v1/institutions/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].active").value(true));

        verify(institutionService).getActiveInstitutions();
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetActiveInstitutions_whenEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(institutionService.getActiveInstitutions()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/institutions/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    // ----------------- CREATE INSTITUTION TESTS --------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testCreateInstitution_success() throws Exception {
        // Arrange
        CreateInstitutionRequest request = createInstitutionRequest();
        InstitutionResponse response = createInstitutionResponse(1L, "USAC", true);

        when(institutionService.createInstitution(any(CreateInstitutionRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/institutions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idInstitution").value(1))
                .andExpect(jsonPath("$.institutionName").value("USAC"))
                .andExpect(jsonPath("$.contactEmail").value("usac@example.com"))
                .andExpect(jsonPath("$.active").value(true));

        verify(institutionService).createInstitution(any(CreateInstitutionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testCreateInstitution_whenNameExists_shouldReturn409() throws Exception {
        // Arrange
        CreateInstitutionRequest request = createInstitutionRequest();

        when(institutionService.createInstitution(any(CreateInstitutionRequest.class)))
                .thenThrow(new DuplicatedEntityException("Institution with the same name already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/institutions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(institutionService).createInstitution(any(CreateInstitutionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testCreateInstitution_whenEmailExists_shouldReturn409() throws Exception {
        // Arrange
        CreateInstitutionRequest request = createInstitutionRequest();

        when(institutionService.createInstitution(any(CreateInstitutionRequest.class)))
                .thenThrow(new DuplicatedEntityException("Institution with the same contact email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/institutions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // --------------- UPDATE INSTITUTION TESTS -------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateInstitution_success() throws Exception {
        // Arrange
        Long institutionId = 1L;
        UpdateInstitutionRequest request = createUpdateRequest();
        InstitutionResponse response = createInstitutionResponse(institutionId, "USAC Updated", true);

        when(institutionService.updateInstitution(eq(institutionId), any(UpdateInstitutionRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/institutions/{id}", institutionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idInstitution").value(institutionId))
                .andExpect(jsonPath("$.institutionName").value("USAC Updated"));

        verify(institutionService).updateInstitution(eq(institutionId), any(UpdateInstitutionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateInstitution_whenNotFound_shouldReturn404() throws Exception {
        // Arrange
        Long institutionId = 999L;
        UpdateInstitutionRequest request = createUpdateRequest();

        when(institutionService.updateInstitution(eq(institutionId), any(UpdateInstitutionRequest.class)))
                .thenThrow(new NotFoundException("Institution not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/institutions/{id}", institutionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateInstitution_whenNameExists_shouldReturn409() throws Exception {
        // Arrange
        Long institutionId = 1L;
        UpdateInstitutionRequest request = createUpdateRequest();

        when(institutionService.updateInstitution(eq(institutionId), any(UpdateInstitutionRequest.class)))
                .thenThrow(new DuplicatedEntityException("Institution with the same name already exists"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/institutions/{id}", institutionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ---------------------- UPDATE INSTITUTION STATUS TESTS ------------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateInstitutionStatus_deactivate_success() throws Exception {
        // Arrange
        Long institutionId = 1L;
        UpdateStatusInstitutionRequest request = new UpdateStatusInstitutionRequest(false);

        doNothing().when(institutionService).uptateStatusInstitution(eq(institutionId), any(UpdateStatusInstitutionRequest.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/institutions/{id}/status", institutionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(institutionService).uptateStatusInstitution(eq(institutionId), any(UpdateStatusInstitutionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateInstitutionStatus_activate_success() throws Exception {
        // Arrange
        Long institutionId = 1L;
        UpdateStatusInstitutionRequest request = new UpdateStatusInstitutionRequest(true);

        doNothing().when(institutionService).uptateStatusInstitution(eq(institutionId), any(UpdateStatusInstitutionRequest.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/institutions/{id}/status", institutionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(institutionService).uptateStatusInstitution(eq(institutionId), any(UpdateStatusInstitutionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateInstitutionStatus_whenNotFound_shouldReturn404() throws Exception {
        // Arrange
        Long institutionId = 999L;
        UpdateStatusInstitutionRequest request = new UpdateStatusInstitutionRequest(false);

        doThrow(new NotFoundException("Institution not found"))
                .when(institutionService).uptateStatusInstitution(eq(institutionId), any(UpdateStatusInstitutionRequest.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/institutions/{id}/status", institutionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateInstitutionStatus_whenHasActiveCongresses_shouldReturn400() throws Exception {
        // Arrange
        Long institutionId = 1L;
        UpdateStatusInstitutionRequest request = new UpdateStatusInstitutionRequest(false);

        doThrow(new BusinessRuleException("Cannot deactivate institution with active congresses"))
                .when(institutionService).uptateStatusInstitution(eq(institutionId), any(UpdateStatusInstitutionRequest.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/institutions/{id}/status", institutionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    // --------------------------- HELPER METHODS -------------------------

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
                "Universidad de San Carlos de Guatemala",
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
                "updated@usac.edu",
                "555-9999"
        );
    }

    private InstitutionResponse createInstitutionResponse(Long id, String name, boolean isActive) {
        return new InstitutionResponse(
                id,
                name,
                "Description for " + name,
                "Address for " + name,
                name.toLowerCase() + "@example.com",
                "555-" + id,
                isActive
        );
    }
}