package com.alessandro.congress_management.controllers.registrationandpayments.registration;

import com.alessandro.congress_management.controllers.registrationandpayments.wallettransaction.WalletTransactionController;
import com.alessandro.congress_management.dto.registrationandpayments.registration.MyCongressRegistrationDTO;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegisteredUserDTO;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegistrationResponse;
import com.alessandro.congress_management.security.CustomUserDetails;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.registrationandpayments.registration.RegistrationService;
import com.alessandro.congress_management.services.registrationandpayments.wallettransaction.WalletTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    //------------ TEST REGISTER FOR CONGRESS ------------
    @Test
    public void testRegisterForCongress_Success() throws Exception {
        //Arrange
        Long congressId = 1L;
        Long userId = 2L;
        String username = "testuser";
        String role = "PARTICIPANT";

        RegistrationResponse mockResponse = new RegistrationResponse(
                1L,
                congressId,
                userId,
                "Test Congress",
                "Test User",
                "emailtest@gmail.com",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );
        when(registrationService.registerForCongress(eq(congressId), eq(userId))).thenReturn(mockResponse);

        // Act
        setupSecurityContext(userId, username, role);
        mockMvc.perform(post("/api/v1/congresses/{congressId}/registrations", congressId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idRegistration").value(mockResponse.getIdRegistration()))
                .andExpect(jsonPath("$.congressId").value(mockResponse.getCongressId()))
                .andExpect(jsonPath("$.userId").value(mockResponse.getUserId()))
                .andExpect(jsonPath("$.congressName").value(mockResponse.getCongressName()))
                .andExpect(jsonPath("$.userName").value(mockResponse.getUserName()))
                .andExpect(jsonPath("$.userEmail").value(mockResponse.getUserEmail()))
                .andExpect(jsonPath("$.amountPaid").value(mockResponse.getAmountPaid().doubleValue()));
    }

    //------------ TEST GET MY REGISTRATIONS ------------
    @Test
    public void testGetMyRegistrations_Success() throws Exception {
        //Arrange
        Long userId = 2L;
        String username = "testuser";
        String role = "PARTICIPANT";

        List<MyCongressRegistrationDTO> mockRegistrations = Arrays.asList(
                createSampleMyCongressRegistrationDTO(1L, 10L),
                createSampleMyCongressRegistrationDTO(2L, 20L)
        );
        when(registrationService.getMyRegistrations(eq(userId))).thenReturn(mockRegistrations);

        // Act
        setupSecurityContext(userId, username, role);
        mockMvc.perform(get("/api/v1/congresses/my-registrations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(mockRegistrations.size()))
                .andExpect(jsonPath("$[0].congressId").value(mockRegistrations.get(0).getCongressId()))
                .andExpect(jsonPath("$[0].congressName").value(mockRegistrations.get(0).getCongressName()))
                .andExpect(jsonPath("$[0].registrationDate").exists())
                .andExpect(jsonPath("$[1].congressId").value(mockRegistrations.get(1).getCongressId()))
                .andExpect(jsonPath("$[1].congressName").value(mockRegistrations.get(1).getCongressName()))
                .andExpect(jsonPath("$[1].registrationDate").exists());
    }

    //------------ TEST GET REGISTRATIONS BY CONGRESS ------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testGetRegistrationsByCongress_Success() throws Exception {
        //Arrange
        Long congressId = 1L;

        List<RegisteredUserDTO> mockRegisteredUsers = Arrays.asList(
                new RegisteredUserDTO(10L, "User One", "email@gmail.com", "org1", new BigDecimal("100.00"), LocalDateTime.now()),
                new RegisteredUserDTO(20L, "User Two", "email2@gmail.com", "org2", new BigDecimal("100.00"), LocalDateTime.now())
        );
        when(registrationService.getRegistrationsByCongress(eq(congressId))).thenReturn(mockRegisteredUsers);

        // Act
        setupSecurityContext(100L, "adminuser", "ADMIN_CONGRESS");
        mockMvc.perform(get("/api/v1/congresses/{congressId}/registrations", congressId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(mockRegisteredUsers.size()))
                .andExpect(jsonPath("$[0].userId").value(mockRegisteredUsers.get(0).getUserId()))
                .andExpect(jsonPath("$[0].fullName").value(mockRegisteredUsers.get(0).getFullName()))
                .andExpect(jsonPath("$[0].email").value(mockRegisteredUsers.get(0).getEmail()))
                .andExpect(jsonPath("$[1].userId").value(mockRegisteredUsers.get(1).getUserId()))
                .andExpect(jsonPath("$[1].fullName").value(mockRegisteredUsers.get(1).getFullName()))
                .andExpect(jsonPath("$[1].email").value(mockRegisteredUsers.get(1).getEmail()));
    }


    //-------- HELPER METHODS --------
    private void setupSecurityContext(Long userId, String username, String role) {
        // Crear CustomUserDetails con el userId
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                username,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );

        // Crear Authentication
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        // Establecer en SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private MyCongressRegistrationDTO createSampleMyCongressRegistrationDTO(Long idCongress, Long registrationId) {
        return new MyCongressRegistrationDTO(
                registrationId,
                idCongress,
                "Sample Congress",
                LocalDate.now(),
                LocalDate.now(),
                "Sample Location",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );
    }
}
