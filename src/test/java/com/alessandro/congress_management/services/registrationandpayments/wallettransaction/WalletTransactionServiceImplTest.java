package com.alessandro.congress_management.services.registrationandpayments.wallettransaction;

import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletBalanceResponse;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletRechargeRequest;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionCreate;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.registrations_and_payments.WalletTransactionEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.wallettransaction.WalletTransactionRepository;
import com.alessandro.congress_management.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletTransactionServiceImplTest {

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    @Spy
    private WalletTransactionServiceImpl walletTransactionService;

    //--------------- TEST CREATE TRANSACTION ---------------
    @Test
    public void testCreateTransaction_recharge_success() throws BusinessRuleException, NotFoundException {
        // Arrange
        UserEntity user = createUser(1L, "testuser");
        user.setWalletBalance(new BigDecimal("50.00"));
        WalletTransactionCreate transaction = createWalletTransactionCreate(user, "RECHARGE", new BigDecimal("100.00"));

        WalletTransactionEntity savedEntity = new WalletTransactionEntity();
        savedEntity.setIdTransaction(1L);


        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any())).thenReturn(savedEntity);
        // Act
        WalletTransactionEntity result =
                walletTransactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), user.getWalletBalance());

        verify(walletTransactionRepository).save(any());
        verify(userRepository).save(user);

    }

    @Test
    void createTransaction_payment_success() throws Exception {

        // Arrange
        UserEntity user = new UserEntity();
        user.setIdUser(1L);
        user.setWalletBalance(new BigDecimal("200.00"));

        WalletTransactionCreate transaction =  createWalletTransactionCreate(user, "PAYMENT", new BigDecimal("50.00"));

        WalletTransactionEntity savedEntity = new WalletTransactionEntity();
        savedEntity.setIdTransaction(2L);

        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any())).thenReturn(savedEntity);

        // Act
        WalletTransactionEntity result =
                walletTransactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), user.getWalletBalance());

        verify(walletTransactionRepository).save(any());
        verify(userRepository).save(user);
    }

    @Test
    void createTransaction_payment_insufficient_balance() {

        // Arrange
        UserEntity user = new UserEntity();
        user.setIdUser(1L);
        user.setWalletBalance(new BigDecimal("30.00"));

        WalletTransactionCreate transaction = createWalletTransactionCreate(user, "PAYMENT", new BigDecimal("50.00"));

        // Act + Assert
        assertThrows(BusinessRuleException.class, () ->
                walletTransactionService.createTransaction(transaction)
        );

        verify(walletTransactionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createTransaction_invalid_type() {

        // Arrange
        UserEntity user = new UserEntity();
        user.setWalletBalance(new BigDecimal("100.00"));

        WalletTransactionCreate transaction = createWalletTransactionCreate(user, "INVALID_TYPE", new BigDecimal("50.00"));

        // Act + Assert
        assertThrows(BusinessRuleException.class, () ->
                walletTransactionService.createTransaction(transaction)
        );

        verify(walletTransactionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    //--------------- TEST WALLET RECHARGE ---------------
    @Test
    void transactionRecharge_success() throws Exception {

        // Arrange
        Long userId = 1L;

        UserEntity user = new UserEntity();
        user.setIdUser(userId);
        user.setWalletBalance(new BigDecimal("100.00"));

        WalletRechargeRequest request = new WalletRechargeRequest(
                new BigDecimal("50.00"),
                "Test recharge",
                LocalDateTime.now()
        );

        WalletTransactionEntity transactionEntity = new WalletTransactionEntity();
        transactionEntity.setIdTransaction(10L);
        transactionEntity.setUser(user);
        transactionEntity.setAmount(request.getAmount());
        transactionEntity.setDescription(request.getDescription());
        transactionEntity.setTransactionType("RECHARGE");

        when(userService.getUserById(userId)).thenReturn(user);

        doReturn(transactionEntity)
                .when(walletTransactionService)
                .createTransaction(any(WalletTransactionCreate.class));

        // Act
        WalletTransactionResponse response =
                walletTransactionService.transactionRecharge(userId, request);

        // Assert
        assertNotNull(response);

        verify(userService).getUserById(userId);
        verify(walletTransactionService)
                .createTransaction(any(WalletTransactionCreate.class));
    }

    @Test
    void transactionRecharge_user_not_found() throws Exception {

        Long userId = 1L;

        WalletRechargeRequest request = new WalletRechargeRequest(
                new BigDecimal("50.00"),
                "Test recharge",
                LocalDateTime.now()
        );

        when(userService.getUserById(userId))
                .thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () ->
                walletTransactionService.transactionRecharge(userId, request)
        );

        verify(walletTransactionService, never())
                .createTransaction(any());
    }

    //--------------- TEST GET WALLET BALANCE ---------------
    @Test
    void getWalletBalance_success() throws Exception {
        // Arrange
        Long userId = 1L;
        UserEntity user = createUser(userId, "testuser");
        user.setWalletBalance(new BigDecimal("150.00"));

        when(userService.getUserById(userId)).thenReturn(user);
        // Act
        WalletBalanceResponse response = walletTransactionService.getWalletBalance(userId);
        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(userId, response.getUserId()),
                () -> assertEquals("Test User", response.getFullName()),
                () -> assertEquals(new BigDecimal("150.00"), response.getBalance())
        );

    }

    @Test
    void getWalletBalance_userNotFound() throws Exception {
        //Arrange
        Long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(new NotFoundException("User not found"));

        //Act + Assert
        assertThrows(NotFoundException.class, () -> walletTransactionService.getWalletBalance(userId));
    }

    //--------------- TEST GET WALLET TRANSACTIONS ---------------
    @Test
    void getWalletTransactions_success() throws Exception {
        // Arrange
        Long userId = 1L;
        UserEntity user = createUser(userId, "testuser");
        WalletTransactionEntity transaction1 = createWalletTransactionEntity(user, "RECHARGE", new BigDecimal("100.00"));
        transaction1.setIdTransaction(1L);
        WalletTransactionEntity transaction2 = createWalletTransactionEntity(user, "PAYMENT", new BigDecimal("50.00"));
        transaction2.setIdTransaction(2L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(walletTransactionRepository.findByUser(user)).thenReturn(List.of(transaction1, transaction2));

        // Act
        List<WalletTransactionResponse> response = walletTransactionService.getWalletTransactions(userId);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(2, response.size()),
                () -> {
                    WalletTransactionResponse r1 = response.get(0);
                    assertEquals(1L, r1.getIdTransaction());
                    assertEquals("RECHARGE", r1.getTransactionType());
                    assertEquals(new BigDecimal("100.00"), r1.getAmount());
                },
                () -> {
                    WalletTransactionResponse r2 = response.get(1);
                    assertEquals(2L, r2.getIdTransaction());
                    assertEquals("PAYMENT", r2.getTransactionType());
                    assertEquals(new BigDecimal("50.00"), r2.getAmount());
                }
        );
    }

    @Test
    void getWalletTransactions_userNotFound() throws Exception {
        //Arrange
        Long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(new NotFoundException("User not found"));

        //Act + Assert
        assertThrows(NotFoundException.class, () -> walletTransactionService.getWalletTransactions(userId));
    }




    //------HELPER METHODS------
    private UserEntity createUser(Long id, String username) {
        UserEntity user = new UserEntity();
        user.setIdUser(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("$2a$10$hashedPassword");
        user.setFullName("Test User");
        user.setIdentificationNumber("12345678");
        user.setPhoneNumber("555-1234");
        user.setOrganization("USAC");
        user.setIsActive(true);
        user.setRole(createRole("PARTICIPANT"));
        return user;
    }

    private RoleEntity createRole(String roleName) {
        RoleEntity role = new RoleEntity();
        role.setIdRole(1);
        role.setRoleName(roleName);
        role.setDescription("Test role");
        return role;
    }

    private WalletTransactionCreate createWalletTransactionCreate(UserEntity user, String type, BigDecimal amount) {
        WalletTransactionCreate transaction = new WalletTransactionCreate(
                user,
                type,
                "Test transaction",
                null,
                java.time.LocalDateTime.now(),
                amount
        );

        return transaction;
    }

    private WalletTransactionEntity createWalletTransactionEntity(UserEntity user, String type, BigDecimal amount) {
        WalletTransactionEntity entity = new WalletTransactionEntity();
        entity.setUser(user);
        entity.setAmount(amount);
        entity.setDescription("Test transaction");
        entity.setTransactionDate(LocalDateTime.now());
        entity.setTransactionType(type);
        return entity;
    }
}
