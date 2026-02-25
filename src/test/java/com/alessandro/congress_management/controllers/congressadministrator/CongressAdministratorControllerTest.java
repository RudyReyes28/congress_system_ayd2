package com.alessandro.congress_management.controllers.congressadministrator;

import com.alessandro.congress_management.controllers.congress.CongressController;
import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congress.CreateCongressRequest;
import com.alessandro.congress_management.dto.congress.UpdateCongressRequest;
import com.alessandro.congress_management.dto.congressadministrator.UserCongressResponse;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.congressadministrator.CongressAdministratorService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CongressAdministratorController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CongressAdministratorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CongressAdministratorService congressAdministratorService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // ---------------- GET CONGRESSES BY ADMIN TESTS --------------

    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testGetCongressesByAdmin_success() throws Exception {
        // Arrange
        Long userId = 1L;
        List<CongressResponse> congresses = Arrays.asList(
                createCongressResponse(1L, "Congress 1", true),
                createCongressResponse(2L, "Congress 2", true)
        );

        when(congressAdministratorService.getCongressesByAdministrator(userId)).thenReturn(congresses);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{idUser}/congresses/administrated", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].congressName").value("Congress 1"))
                .andExpect(jsonPath("$[1].congressName").value("Congress 2"));

    }

    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testGetCongressesByAdmin_whenNoCongressesFound_shouldReturn404() throws Exception {
        // Arrange
        Long userId = 999L;

        when(congressAdministratorService.getCongressesByAdministrator(userId))
                .thenThrow(new NotFoundException("No congresses found for user with id: " + userId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{idUser}/congresses/administrated", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testGetCongressesByAdmin_whenEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        Long userId = 1L;

        when(congressAdministratorService.getCongressesByAdministrator(userId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{idUser}/congresses/administrated", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    //----------------- GET ADMINISTRATORS BY CONGRESS TESTS --------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testGetAdministratorsByCongress_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        List<UserCongressResponse> administrators = Arrays.asList(
                createUserCongressResponse(1L, "admin1", "admin1@gmail.com"),
                createUserCongressResponse(2L, "admin2", "admin2@gmail.com")
        );

        when(congressAdministratorService.getAdministratorsByCongress(congressId)).thenReturn(administrators);

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses/{idCongress}/administrators", congressId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("admin1"))
                .andExpect(jsonPath("$[0].email").value("admin1@gmail.com"))
                .andExpect(jsonPath("$[1].username").value("admin2"))
                .andExpect(jsonPath("$[1].email").value("admin2@gmail.com"));

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

    private UserCongressResponse createUserCongressResponse(Long idUser, String username, String email) {
        return new UserCongressResponse(
                idUser,
                username,
                email,
                "Full Name",
                "1234567890",
                "Organization",
                "ID12345",
                "Institution Name",
                LocalDate.now().atStartOfDay()
        );
    }
}
