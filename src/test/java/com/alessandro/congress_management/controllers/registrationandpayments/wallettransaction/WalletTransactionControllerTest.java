package com.alessandro.congress_management.controllers.registrationandpayments.wallettransaction;

import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletBalanceResponse;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletRechargeRequest;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionResponse;
import com.alessandro.congress_management.security.CustomUserDetails;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.registrationandpayments.wallettransaction.WalletTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import com.alessandro.congress_management.controllers.congress.CongressController;

@WebMvcTest(WalletTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WalletTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletTransactionService walletTransactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    //-------------- TEST FOR CREATE WALLET RECHARGE --------------

    @Test
    void rechargeWallet_success() throws Exception {
        // Arrange
        Long userId = 1L;

        WalletRechargeRequest request = new WalletRechargeRequest(
                new BigDecimal("100.00"),
                "Test recharge",
                LocalDateTime.now().plusMinutes(1)
        );

        WalletTransactionResponse response = new WalletTransactionResponse(
                1L,
                userId,
                "John Doe",
                "RECHARGE",
                "Test recharge",
                null,
                request.getTransactionDate(),
                request.getAmount()
        );

        when(walletTransactionService.transactionRecharge(eq(userId), any(WalletRechargeRequest.class)))
                .thenReturn(response);

        setupSecurityContext(userId, "johndoe", "PARTICIPANT");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/wallet/recharge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTransaction").value(1L))
                .andExpect(jsonPath("$.transactionType").value("RECHARGE"))
                .andExpect(jsonPath("$.amount").value("100.0"));

        verify(walletTransactionService).transactionRecharge(eq(userId), any(WalletRechargeRequest.class));
    }

    //-------------- TEST FOR GET WALLET BALANCE --------------
    @Test
    void getWalletBalance_success() throws Exception {
        Long userId = 1L;

        WalletBalanceResponse response = new WalletBalanceResponse(
                userId,
                "John Doe",
                "john@email.com",
                new BigDecimal("500.00")
        );

        when(walletTransactionService.getWalletBalance(anyLong()))
                .thenReturn(response);

        setupSecurityContext(userId, "johndoe", "PARTICIPANT");
        mockMvc.perform(get("/api/v1/users/wallet/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));

        verify(walletTransactionService).getWalletBalance(anyLong());
    }

    @Test
    void getWalletTransactions_success() throws Exception {

        WalletTransactionResponse tx = new WalletTransactionResponse(
                1L,
                1L,
                "John Doe",
                "RECHARGE",
                "Test recharge",
                null,
                LocalDateTime.now(),
                new BigDecimal("100.00")
        );

        when(walletTransactionService.getWalletTransactions(anyLong()))
                .thenReturn(List.of(tx));

        setupSecurityContext(1L, "johndoe", "PARTICIPANT");

        mockMvc.perform(get("/api/v1/users/wallet/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionType").value("RECHARGE"));

        verify(walletTransactionService).getWalletTransactions(anyLong());
    }

    //-------- HELPER METHODS --------
    private WalletRechargeRequest createSampleWalletRechargeRequest() {

        WalletRechargeRequest request = new WalletRechargeRequest(
                new BigDecimal("50.00"),
                "Recharge for conference registration",
                LocalDateTime.now()
        );

        return request;
    }


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

}
