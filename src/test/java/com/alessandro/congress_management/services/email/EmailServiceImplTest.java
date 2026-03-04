package com.alessandro.congress_management.services.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl Tests")
class EmailServiceImplTest {

    @Mock private JavaMailSender mailSender;
    @Mock private MimeMessage mimeMessage;

    private EmailTemplateBuilder templateBuilder;
    private EmailServiceImpl service;

    @BeforeEach
    void setUp() {
        templateBuilder = new EmailTemplateBuilder();
        service = new EmailServiceImpl(mailSender, templateBuilder);

        ReflectionTestUtils.setField(service, "fromAddress", "noreply@congress.com");
        ReflectionTestUtils.setField(service, "fromName",    "Congress Management");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    //------------------ TESTS FOR SEND WELCOME EMAILS ------------------

    @Test
    void sendWelcomeEmail_callsMailSenderSend() {
        service.sendWelcomeEmail("user@mail.com", "Ana López", "ana.lopez", "pass123", "Invited Speaker");
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendWelcomeEmail_createsMimeMessage() {
        service.sendWelcomeEmail("user@mail.com", "Ana López", "ana.lopez", "pass123", "Any");
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendWelcomeEmail_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                service.sendWelcomeEmail("user@mail.com", "Ana", "ana", "pass", "Test")
        );
    }

    @Test
    void sendWelcomeEmail_doesNotPropagateException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP down"));

        assertThatNoException().isThrownBy(() ->
                service.sendWelcomeEmail("user@mail.com", "Ana", "ana", "pass", "Test")
        );
    }

   //------------------- TEST SEND CONGRESS ADMIN ASSIGNMENT EMAILS -------------------


    @Test
    void sendCongressAdminAssignmentEmail_callsMailSenderSend() {
        service.sendCongressAdminAssignmentEmail("admin@mail.com", "Carlos Ruiz", "Tech Congress 2026");
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("should not throw when mail sender succeeds")
    void sendCongressAdminAssignmentEmail_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                service.sendCongressAdminAssignmentEmail("admin@mail.com", "Carlos", "My Congress")
        );
    }

    @Test
    @DisplayName("should not propagate exception when mail sender fails")
    void sendCongressAdminAssignmentEmail_doesNotPropagateException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));

        assertThatNoException().isThrownBy(() ->
                service.sendCongressAdminAssignmentEmail("admin@mail.com", "Carlos", "My Congress")
        );
    }

    //---------------- TESTS SEND SCIENTIFIC COMMITTEE EMAILS ----------------

    @Test
    void sendScientificCommitteeEmail_callsMailSenderSend() {
        service.sendScientificCommitteeEmail("dr@mail.com", "Dr. García", "AI Summit");
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendScientificCommitteeEmail_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                service.sendScientificCommitteeEmail("dr@mail.com", "Dr. García", "AI Summit")
        );
    }

    @Test
    void sendScientificCommitteeEmail_doesNotPropagateException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));

        assertThatNoException().isThrownBy(() ->
                service.sendScientificCommitteeEmail("dr@mail.com", "Dr. García", "AI Summit")
        );
    }

    // -------------- TESTS FOR SEND PRESENTER ACCEPTED EMAILS ----------------

    @Test
    @DisplayName("should call mailSender.send() once for a regular presenter")
    void sendPresenterAcceptedEmail_callsSendForRegularPresenter() {
        service.sendPresenterAcceptedEmail(
                "speaker@mail.com", "Luis Torres", "Tech Congress", "ML Talk", "PONENCIA", false);
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("should call mailSender.send() once for an invited speaker")
    void sendPresenterAcceptedEmail_callsSendForInvitedSpeaker() {
        service.sendPresenterAcceptedEmail(
                "guest@mail.com", "Guest User", "Tech Congress", "Keynote", "PONENCIA", true);
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("should not throw for PONENCIA type")
    void sendPresenterAcceptedEmail_doesNotThrowForPonencia() {
        assertThatNoException().isThrownBy(() ->
                service.sendPresenterAcceptedEmail(
                        "s@mail.com", "Name", "Congress", "Talk", "PONENCIA", false)
        );
    }

    @Test
    @DisplayName("should not throw for TALLER type")
    void sendPresenterAcceptedEmail_doesNotThrowForTaller() {
        assertThatNoException().isThrownBy(() ->
                service.sendPresenterAcceptedEmail(
                        "s@mail.com", "Name", "Congress", "Workshop", "TALLER", false)
        );
    }

    @Test
    @DisplayName("should not propagate exception when mail sender fails")
    void sendPresenterAcceptedEmail_doesNotPropagateException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));

        assertThatNoException().isThrownBy(() ->
                service.sendPresenterAcceptedEmail(
                        "s@mail.com", "Name", "Congress", "Talk", "PONENCIA", false)
        );
    }

    // ------------------- TEST FOR GENERAL SEND BEHAVIOR -------------------

    @Test
    void GeneralBehavior_exactlyOneSendPerMethod() {
        service.sendWelcomeEmail("a@b.com", "A", "a", "p", "r");
        service.sendCongressAdminAssignmentEmail("a@b.com", "A", "C");
        service.sendScientificCommitteeEmail("a@b.com", "A", "C");
        service.sendPresenterAcceptedEmail("a@b.com", "A", "C", "Act", "PONENCIA", false);

        verify(mailSender, times(4)).send(mimeMessage);
    }

    @Test
    void GeneralBehavior_createsMimeMessageForEachCall() {
        service.sendWelcomeEmail("a@b.com", "A", "a", "p", "r");
        service.sendCongressAdminAssignmentEmail("a@b.com", "A", "C");

        verify(mailSender, times(2)).createMimeMessage();
    }
}