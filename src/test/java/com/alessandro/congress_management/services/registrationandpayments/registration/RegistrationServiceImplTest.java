package com.alessandro.congress_management.services.registrationandpayments.registration;

import com.alessandro.congress_management.dto.registrationandpayments.registration.MyCongressRegistrationDTO;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegisteredUserDTO;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegistrationResponse;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.models.registrations_and_payments.RegistrationEntity;
import com.alessandro.congress_management.models.registrations_and_payments.WalletTransactionEntity;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.registrationandpayments.wallettransaction.WalletTransactionService;
import com.alessandro.congress_management.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceImplTest {
    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private CongressService congressService;
    @Mock
    private UserService userService;
    @Mock
    private WalletTransactionService walletTransactionService;
    @InjectMocks
    private RegistrationServiceImpl registrationService;

    //------------------- TEST REGISTER FOR CONGRESS -------------------
    @Test
    void testRegisterForCongress_Success() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress", true);
        UserEntity user = createUser(userId, "testuser");
        BigDecimal initialBalance = new BigDecimal("200.00");
        user.setWalletBalance(initialBalance);

        RegistrationEntity registrationEntity = createRegistrationEntity(1L, congress, user, congress.getPrice());

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(userService.getUserById(userId)).thenReturn(user);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(userId, congressId)).thenReturn(false);
        when(registrationRepository.save(any(RegistrationEntity.class))).thenReturn(registrationEntity);
        when(walletTransactionService.createTransaction(any())).thenReturn(createWalletTransactionEntity(user, "PAYMENT", congress.getPrice()));

        // Act
        RegistrationResponse response = registrationService.registerForCongress(congressId, userId);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(registrationEntity.getIdRegistration(), response.getIdRegistration()),
                () -> assertEquals(congressId, response.getCongressId()),
                () -> assertEquals(userId, response.getUserId()),
                () -> assertEquals(congress.getCongressName(), response.getCongressName()),
                () -> assertEquals(user.getFullName(), response.getUserName()),
                () -> assertEquals(user.getEmail(), response.getUserEmail()),
                () -> assertEquals(congress.getPrice(), response.getAmountPaid()),
                () -> assertNotNull(response.getRegistrationDate())
        );
    }

    @Test
    void testRegisterForCongress_congressNotActive() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress", false);

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> registrationService.registerForCongress(congressId, userId));
        assertEquals("The congress is not active.", exception.getMessage());
    }

    @Test
    void testRegisterForCongress_userAlreadyRegistered() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress", true);
        UserEntity user = createUser(userId, "testuser");

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(userService.getUserById(userId)).thenReturn(user);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(userId, congressId)).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> registrationService.registerForCongress(congressId, userId));
        assertEquals("User is already registered for this congress.", exception.getMessage());
    }

    @Test
    void testRegisterForCongress_insufficientBalance() throws Exception {
        // Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress", true);
        UserEntity user = createUser(userId, "testuser");
        user.setWalletBalance(new BigDecimal("100.00")); // Menos que el precio del congreso

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(userService.getUserById(userId)).thenReturn(user);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(userId, congressId)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> registrationService.registerForCongress(congressId, userId));
        assertEquals("Insufficient balance in wallet.", exception.getMessage());
    }

    //------------------ TEST GET MY REGISTRATION ------------------
    @Test
    void testGetRegistrationById_Success() throws Exception {
        // Arrange
        Long registrationId = 1L;
        CongressEntity congress = createCongress(1L, "Test Congress", true);
        Long registrationId2 = 2L;
        CongressEntity congress2 = createCongress(2L, "Test Congress 2", true);

        UserEntity user = createUser(1L, "testuser");
        RegistrationEntity registrationEntity1 = createRegistrationEntity(registrationId, congress, user, congress.getPrice());
        RegistrationEntity registrationEntity2 = createRegistrationEntity(registrationId2, congress2, user, congress2.getPrice());

        when(registrationRepository.findByUser_IdUser(user.getIdUser())).thenReturn(List.of(registrationEntity1, registrationEntity2));

        // Act
        List<MyCongressRegistrationDTO> response = registrationService.getMyRegistrations(user.getIdUser());

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(2, response.size()),
                () -> {
                    MyCongressRegistrationDTO dto1 = response.get(0);
                    assertEquals(registrationEntity1.getIdRegistration(), dto1.getRegistrationId());
                    assertEquals(congress.getIdCongress(), dto1.getCongressId());
                    assertEquals(congress.getCongressName(), dto1.getCongressName());
                    assertEquals(congress.getPrice(), dto1.getAmountPaid());
                    assertNotNull(dto1.getRegistrationDate());
                },
                () -> {
                    MyCongressRegistrationDTO dto2 = response.get(1);
                    assertEquals(registrationEntity2.getIdRegistration(), dto2.getRegistrationId());
                    assertEquals(congress2.getIdCongress(), dto2.getCongressId());
                    assertEquals(congress2.getCongressName(), dto2.getCongressName());
                    assertEquals(congress2.getPrice(), dto2.getAmountPaid());
                    assertNotNull(dto2.getRegistrationDate());
                }
        );
    }

    //--------------------- TEST GET REGISTRATIONS BY CONGRESS---------------------
    @Test
    void testGetRegistrationsByCongressId_Success() throws Exception {
        // Arrange
        Long congressId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress", true);
        UserEntity user1 = createUser(1L, "testuser1");
        UserEntity user2 = createUser(2L, "testuser2");

        RegistrationEntity registrationEntity1 = createRegistrationEntity(1L, congress, user1, congress.getPrice());
        RegistrationEntity registrationEntity2 = createRegistrationEntity(2L, congress, user2, congress.getPrice());

        when(registrationRepository.findByCongress_IdCongress(congressId)).thenReturn(List.of(registrationEntity1, registrationEntity2));

        // Act
        List<RegisteredUserDTO> response = registrationService.getRegistrationsByCongress(congressId);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(2, response.size()),
                () -> {
                    RegisteredUserDTO dto1 = response.get(0);
                    assertEquals(user1.getIdUser(), dto1.getUserId());
                    assertEquals(user1.getFullName(), dto1.getFullName());
                    assertEquals(user1.getEmail(), dto1.getEmail());
                },
                () -> {
                    RegisteredUserDTO dto2 = response.get(1);
                    assertEquals(user2.getIdUser(), dto2.getUserId());
                    assertEquals(user2.getFullName(), dto2.getFullName());
                    assertEquals(user2.getEmail(), dto2.getEmail());
                }
        );
    }



    //------- Helper Methods ------------
    private CongressEntity createCongress(Long id, String name, boolean isActive) {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(id);
        congress.setCongressName(name);
        congress.setDescription("Description for " + name);
        congress.setStartDate(LocalDate.of(2026, 5, 15));
        congress.setEndDate(LocalDate.of(2026, 5, 17));
        congress.setLocation("Guatemala City");
        congress.setPrice(new BigDecimal("150.00"));
        congress.setIsActive(isActive);

        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(1L);
        institution.setInstitutionName("USAC");
        institution.setIsActive(true);
        congress.setInstitution(institution);

        return congress;
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

    private RegistrationEntity createRegistrationEntity(Long id, CongressEntity congress, UserEntity user, BigDecimal amountPaid) {
        RegistrationEntity registration = new RegistrationEntity();
        registration.setIdRegistration(id);
        registration.setCongress(congress);
        registration.setUser(user);
        registration.setAmountPaid(amountPaid);
        registration.setRegistrationDate(LocalDateTime.now());
        return registration;
    }
}
