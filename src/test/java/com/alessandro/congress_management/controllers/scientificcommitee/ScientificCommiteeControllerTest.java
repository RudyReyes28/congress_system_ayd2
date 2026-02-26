package com.alessandro.congress_management.controllers.scientificcommitee;

import com.alessandro.congress_management.dto.scientificcommitee.EligibleUserCommitteeResponse;
import com.alessandro.congress_management.dto.scientificcommitee.ScientificCommiteeResponse;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.congress_management.ScientificCommiteeEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.congressadministrator.CongressAdministratorService;
import com.alessandro.congress_management.services.scientificcommitee.ScientificCommiteeService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScientificCommiteeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ScientificCommiteeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScientificCommiteeService scientificCommiteeService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    //------------------- TESTS CREATE SCIENTIFIC COMMITTEE MEMBER ------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testCreateScientificCommiteeMember_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 2L;

        ScientificCommiteeEntity committeeMember = createScientificCommiteeEntity(congressId, userId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/congresses/{congressId}/committee/{userId}", congressId, userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    //-------------------- TESTS GET SCIENTIFIC COMMITTEE MEMBERS BY CONGRESS ID ------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testGetScientificCommiteeMembersByCongressId_success() throws Exception {
        // Arrange

        List<ScientificCommiteeResponse> committeeMembers = Arrays.asList(
                createScientificCommiteeResponse(1L,"testuser1", "Test User 1", "testuser1@gmail.com"),
                createScientificCommiteeResponse(2L,"testuser2", "Test User 2", "testuser2@gmail.com")
        );

        when(scientificCommiteeService.getScientificCommiteeMembersByCongressId(1L)).thenReturn(committeeMembers);

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses/{congressId}/committee", 1L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("testuser1"))
                .andExpect(jsonPath("$[0].fullName").value("Test User 1"))
                .andExpect(jsonPath("$[0].email").value("testuser1@gmail.com"));
    }

    //-------------------- TESTS GET ELIGIBLE USERS FOR COMMITTEE ------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testGetEligibleUsersForCommittee_success() throws Exception {
        // Arrange
        List<EligibleUserCommitteeResponse> eligibleUsers = Arrays.asList(
                new EligibleUserCommitteeResponse(2L, "Test User 1", "Organization 1"),
                new EligibleUserCommitteeResponse(3L, "Test User 2", "Organization 2")
        );

        when(scientificCommiteeService.getEligibleUsersForCommittee(1L)).thenReturn(eligibleUsers);

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses/{congressId}/committee/eligible-users", 1L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idUser").value(2L))
                .andExpect(jsonPath("$[0].fullName").value("Test User 1"))
                .andExpect(jsonPath("$[0].organization").value("Organization 1"));
    }

    //-------------------- TESTS REMOVE SCIENTIFIC COMMITTEE MEMBER ------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    void testRemoveScientificCommiteeMember_success() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 2L;

        // Act & Assert
        mockMvc.perform(delete("/api/v1/congresses/{congressId}/committee/{userId}", congressId, userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(scientificCommiteeService).deleteScientificCommiteeMember(congressId, userId);
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

    private ScientificCommiteeEntity createScientificCommiteeEntity(Long congressId, Long userId) {
        ScientificCommiteeEntity committeeMember = new ScientificCommiteeEntity();
        committeeMember.setCongress(createCongress(congressId, "Test Congress"));
        committeeMember.setUser(createUser(userId, "testuser"));
        return committeeMember;
    }

    private ScientificCommiteeResponse createScientificCommiteeResponse( Long idUser, String username,String fullName, String email) {
        return new ScientificCommiteeResponse(
                idUser,
                username,
                email,
                fullName,
                "1234567890",
                "Organization",
                "ID12345",
                "Institution Name",
                "Congress Name",
                LocalDate.now().atStartOfDay()
        );

    }
}
