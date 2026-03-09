package com.alessandro.congress_management.services.invitation;

import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserInvitationEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.invitation.UserInvitationRepository;
import com.alessandro.congress_management.services.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserInvitationServiceImplTest {


    private static final Long   ID_USER      = 1L;
    private static final String USER_EMAIL   = "ana@mail.com";
    private static final String USER_NAME    = "Ana López";
    private static final String FRONTEND_URL = "https://congreso.usac.gt";
    private static final String VALID_TOKEN  = "abc123def456abc123def456abc123de";
    private static final String NEW_PASSWORD = "SecurePass123";
    private static final String ENCODED_PWD  = "$2a$10$hashedpassword";


    @Mock private UserInvitationRepository invitationRepository;
    @Mock private UserRepository           userRepository;
    @Mock private PasswordEncoder          passwordEncoder;
    @Mock private EmailService             emailService;

    @InjectMocks
    private UserInvitationServiceImpl service;



    @Test
    void testSendInvitation_SavesTokenAndSendsEmail() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(service, "frontendUrl", FRONTEND_URL);
        ArgumentCaptor<UserInvitationEntity> captor = ArgumentCaptor.forClass(UserInvitationEntity.class);

        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));

        // Act
        service.sendInvitation(ID_USER);

        // Assert
        assertAll(
                () -> verify(invitationRepository).deleteByUser_IdUserAndUsedAtIsNull(ID_USER),
                () -> verify(invitationRepository).save(captor.capture()),
                () -> assertNotNull(captor.getValue().getToken()),
                () -> assertFalse(captor.getValue().getToken().isBlank()),
                () -> assertEquals(user().getIdUser(), captor.getValue().getUser().getIdUser()),
                () -> assertTrue(captor.getValue().getExpiresAt().isAfter(LocalDateTime.now())),
                () -> verify(emailService).sendInvitationEmail(
                        eq(USER_EMAIL), eq(USER_NAME), contains(FRONTEND_URL + "/activate?token="))
        );
    }

    @Test
    void testSendInvitation_InvalidatesPreviousTokenBeforeCreatingNew() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(service, "frontendUrl", FRONTEND_URL);
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));

        // Act
        service.sendInvitation(ID_USER);

        // Assert —
        var inOrder = inOrder(invitationRepository);
        inOrder.verify(invitationRepository).deleteByUser_IdUserAndUsedAtIsNull(ID_USER);
        inOrder.verify(invitationRepository).save(any());
    }

    @Test
    void testSendInvitation_TokenIsUniqueEachCall() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(service, "frontendUrl", FRONTEND_URL);
        when(userRepository.findById(ID_USER)).thenReturn(Optional.of(user()));
        ArgumentCaptor<UserInvitationEntity> captor = ArgumentCaptor.forClass(UserInvitationEntity.class);

        // Act — send twice
        service.sendInvitation(ID_USER);
        service.sendInvitation(ID_USER);

        // Assert — two saves with different tokens
        verify(invitationRepository, times(2)).save(captor.capture());
        String token1 = captor.getAllValues().get(0).getToken();
        String token2 = captor.getAllValues().get(1).getToken();
        assertNotEquals(token1, token2);
    }

    @Test
    void testSendInvitation_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(ID_USER)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> service.sendInvitation(ID_USER));

        verifyNoInteractions(emailService);
        verify(invitationRepository, never()).save(any());
    }


    @Test
    void testActivateAccount_ActivatesUserAndMarksTokenUsed() throws Exception {
        // Arrange
        ArgumentCaptor<UserEntity>           userCaptor       = ArgumentCaptor.forClass(UserEntity.class);
        ArgumentCaptor<UserInvitationEntity> invitationCaptor = ArgumentCaptor.forClass(UserInvitationEntity.class);

        UserEntity user = user();
        user.setIsActive(false);
        user.setPassword(null);

        when(invitationRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(validInvitation(user)));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PWD);

        // Act
        service.activateAccount(VALID_TOKEN, NEW_PASSWORD, NEW_PASSWORD);

        // Assert
        assertAll(
                () -> verify(userRepository).save(userCaptor.capture()),
                () -> assertTrue(userCaptor.getValue().getIsActive()),
                () -> assertEquals(ENCODED_PWD, userCaptor.getValue().getPassword()),
                () -> verify(invitationRepository).save(invitationCaptor.capture()),
                () -> assertNotNull(invitationCaptor.getValue().getUsedAt())
        );
    }

    @Test
    void testActivateAccount_WhenTokenNotFound() {
        // Arrange
        when(invitationRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.activateAccount(VALID_TOKEN, NEW_PASSWORD, NEW_PASSWORD));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void testActivateAccount_WhenTokenAlreadyUsed() {
        // Arrange
        UserInvitationEntity usedInvitation = validInvitation(user());
        usedInvitation.setUsedAt(LocalDateTime.now().minusDays(1)); // already used

        when(invitationRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(usedInvitation));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.activateAccount(VALID_TOKEN, NEW_PASSWORD, NEW_PASSWORD));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void testActivateAccount_WhenTokenExpired() {
        // Arrange
        UserInvitationEntity expiredInvitation = validInvitation(user());
        expiredInvitation.setExpiresAt(LocalDateTime.now().minusHours(1)); // expired

        when(invitationRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(expiredInvitation));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.activateAccount(VALID_TOKEN, NEW_PASSWORD, NEW_PASSWORD));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void testActivateAccount_WhenPasswordsDoNotMatch() {
        // Arrange
        when(invitationRepository.findByToken(VALID_TOKEN))
                .thenReturn(Optional.of(validInvitation(user())));

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.activateAccount(VALID_TOKEN, NEW_PASSWORD, "OtherPassword1"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void testActivateAccount_UsedAtIsSetToCurrentTime() throws Exception {
        // Arrange
        ArgumentCaptor<UserInvitationEntity> captor = ArgumentCaptor.forClass(UserInvitationEntity.class);
        when(invitationRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(validInvitation(user())));
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PWD);

        LocalDateTime before = LocalDateTime.now();

        // Act
        service.activateAccount(VALID_TOKEN, NEW_PASSWORD, NEW_PASSWORD);

        LocalDateTime after = LocalDateTime.now();

        // Assert — usedAt is stamped between before and after
        verify(invitationRepository).save(captor.capture());
        assertAll(
                () -> assertNotNull(captor.getValue().getUsedAt()),
                () -> assertFalse(captor.getValue().getUsedAt().isBefore(before)),
                () -> assertFalse(captor.getValue().getUsedAt().isAfter(after))
        );
    }


    private UserEntity user() {
        UserEntity u = new UserEntity();
        u.setIdUser(ID_USER);
        u.setFullName(USER_NAME);
        u.setEmail(USER_EMAIL);
        u.setUsername("ana.lopez");
        u.setIsActive(false);
        u.setPassword(null);
        return u;
    }

    private UserInvitationEntity validInvitation(UserEntity user) {
        UserInvitationEntity inv = new UserInvitationEntity();
        inv.setUser(user);
        inv.setToken(VALID_TOKEN);
        inv.setExpiresAt(LocalDateTime.now().plusHours(48));
        inv.setUsedAt(null);
        return inv;
    }
}