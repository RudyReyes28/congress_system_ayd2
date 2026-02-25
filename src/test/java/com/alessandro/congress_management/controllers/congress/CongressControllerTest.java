package com.alessandro.congress_management.controllers.congress;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congress.CreateCongressRequest;
import com.alessandro.congress_management.dto.congress.UpdateCongressRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.congress.CongressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CongressController.class)
@AutoConfigureMockMvc(addFilters = false)
class CongressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CongressService congressService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // ------------------ CREATE CONGRESS TESTS -------------------

    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testCreateCongress_success() throws Exception {
        // Arrange
        CreateCongressRequest request = createCongressRequest();
        CongressResponse response = createCongressResponse(1L, "Tech Congress 2026", true);

        when(congressService.createCongress(any(CreateCongressRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/congresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCongress").value(1))
                .andExpect(jsonPath("$.congressName").value("Tech Congress 2026"))
                .andExpect(jsonPath("$.institutionName").value("USAC"))
                .andExpect(jsonPath("$.active").value(true));

        verify(congressService).createCongress(any(CreateCongressRequest.class));
    }



    // ------------------ UPDATE CONGRESS TESTS --------------------

    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testUpdateCongress_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        UpdateCongressRequest request = createUpdateRequest();
        CongressResponse response = createCongressResponse(congressId, "Updated Congress", true);

        when(congressService.updateCongress(eq(congressId), any(UpdateCongressRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/congresses/{idCongress}", congressId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCongress").value(congressId))
                .andExpect(jsonPath("$.congressName").value("Updated Congress"));

        verify(congressService).updateCongress(eq(congressId), any(UpdateCongressRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testUpdateCongress_whenNotFound_shouldReturn404() throws Exception {
        // Arrange
        Long congressId = 999L;
        UpdateCongressRequest request = createUpdateRequest();

        when(congressService.updateCongress(eq(congressId), any(UpdateCongressRequest.class)))
                .thenThrow(new NotFoundException("Congress not found with id: " + congressId));

        // Act & Assert
        mockMvc.perform(put("/api/v1/congresses/{idCongress}", congressId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ---------------------- GET ALL CONGRESSES TESTS --------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetAllCongresses_success() throws Exception {
        // Arrange
        List<CongressResponse> congresses = Arrays.asList(
                createCongressResponse(1L, "Congress 1", true),
                createCongressResponse(2L, "Congress 2", false),
                createCongressResponse(3L, "Congress 3", true)
        );

        when(congressService.getAllCongresses()).thenReturn(congresses);

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].congressName").value("Congress 1"))
                .andExpect(jsonPath("$[1].congressName").value("Congress 2"))
                .andExpect(jsonPath("$[2].congressName").value("Congress 3"));

        verify(congressService).getAllCongresses();
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetAllCongresses_whenEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(congressService.getAllCongresses()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    // -------------------- GET ACTIVE CONGRESSES (PUBLIC) TESTS ----------------

    @Test
    void testGetActiveCongresses_public_success() throws Exception {
        // Arrange
        List<CongressResponse> activeCongresses = Arrays.asList(
                createCongressResponse(1L, "Active Congress 1", true),
                createCongressResponse(2L, "Active Congress 2", true)
        );

        when(congressService.getActiveCongresses()).thenReturn(activeCongresses);

        // Act & Assert - Sin autenticación (público)
        mockMvc.perform(get("/api/v1/congresses/public")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].active").value(true));

        verify(congressService).getActiveCongresses();
    }

    @Test
    @WithMockUser(roles = "PARTICIPANT")
    void testGetActiveCongresses_withAuthentication_shouldAlsoWork() throws Exception {
        // Arrange
        List<CongressResponse> activeCongresses = Arrays.asList(
                createCongressResponse(1L, "Active Congress", true)
        );

        when(congressService.getActiveCongresses()).thenReturn(activeCongresses);

        // Act & Assert - Con autenticación también funciona (es público)
        mockMvc.perform(get("/api/v1/congresses/public")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetActiveCongresses_whenEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(congressService.getActiveCongresses()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses/public")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    //----------------- ADD ADMINISTRATOR TO CONGRESS TESTS ---------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testAddAdministratorToCongress_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 2L;

        doNothing().when(congressService).addAdministrator(congressId, userId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/congresses/{idCongress}/administrators/{idUser}", congressId, userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(congressService).addAdministrator(congressId, userId);
    }

    //----------------- REMOVE ADMINISTRATOR FROM CONGRESS TESTS ---------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testRemoveAdministratorFromCongress_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 2L;

        doNothing().when(congressService).removeAdministrator(congressId, userId);
        // Act & Assert
        mockMvc.perform(delete("/api/v1/congresses/{idCongress}/administrators/{idUser}", congressId, userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(congressService).removeAdministrator(congressId, userId);
    }




    // -----------------HELPER METHODS---------------------

    private CreateCongressRequest createCongressRequest() {
        return new CreateCongressRequest(
                1L,
                "Tech Congress 2026",
                "A technology conference for developers",
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

    private CongressResponse createCongressResponse(Long id, String name, boolean isActive) {
        return new CongressResponse(
                id,
                "USAC",
                name,
                "Description for " + name,
                "2026-05-15",
                "2026-05-17",
                "Guatemala City",
                "150.00",
                isActive
        );
    }
}