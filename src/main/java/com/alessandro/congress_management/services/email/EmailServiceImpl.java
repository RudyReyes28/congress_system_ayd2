package com.alessandro.congress_management.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder templateBuilder;

    @Value("${app.mail.from-address}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailServiceImpl(JavaMailSender mailSender, EmailTemplateBuilder templateBuilder) {
        this.mailSender = mailSender;
        this.templateBuilder = templateBuilder;
    }


    @Async
    @Override
    public void sendWelcomeEmail(String to, String fullName, String username,
                                 String rawPassword, String reason) {
        String subject = "Welcome to Congress Management — Your account is ready";
        String html    = templateBuilder.buildWelcomeEmail(fullName, username, rawPassword, reason);
        send(to, subject, html);
    }

    @Async
    @Override
    public void sendCongressAdminAssignmentEmail(String to, String fullName, String congressName) {
        String subject = "You've been assigned as Congress Administrator — " + congressName;
        String html    = templateBuilder.buildCongressAdminEmail(fullName, congressName);
        send(to, subject, html);
    }

    @Async
    @Override
    public void sendScientificCommitteeEmail(String to, String fullName, String congressName) {
        String subject = "Scientific Committee Invitation — " + congressName;
        String html    = templateBuilder.buildScientificCommitteeEmail(fullName, congressName);
        send(to, subject, html);
    }

    @Async
    @Override
    public void sendPresenterAcceptedEmail(String to, String fullName, String congressName,
                                           String activityName, String activityType,
                                           boolean isInvited) {
        String subject = isInvited
                ? "You've been invited as speaker — " + congressName
                : "Your submission was accepted — " + congressName;
        String html = templateBuilder.buildPresenterAcceptedEmail(
                fullName, congressName, activityName, activityType, isInvited);
        send(to, subject, html);
    }


    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromAddress, fromName));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = html

            mailSender.send(message);
            log.info("Email sent → to={} subject='{}'", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to build email → to={} subject='{}' error={}", to, subject, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email → to={} subject='{}' error={}", to, subject, e.getMessage(), e);
        }
    }
}