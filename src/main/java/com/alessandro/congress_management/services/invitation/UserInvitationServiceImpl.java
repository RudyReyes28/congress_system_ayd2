package com.alessandro.congress_management.services.invitation;

import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserInvitationEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.invitation.UserInvitationRepository;
import com.alessandro.congress_management.services.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserInvitationServiceImpl implements UserInvitationService {

    private static final long TOKEN_EXPIRY_HOURS = 48;

    private final UserInvitationRepository invitationRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public UserInvitationServiceImpl(UserInvitationRepository invitationRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void sendInvitation(Long idUser) throws NotFoundException {
        UserEntity user = userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Invalidar si el usuario ya tiene una invitación activa y eliminarla
        invitationRepository.deleteByUser_IdUserAndUsedAtIsNull(idUser);

        // Crear nueva invitacion
        String token = UUID.randomUUID().toString().replace("-", "");

        UserInvitationEntity invitation = new UserInvitationEntity();
        invitation.setUser(user);
        invitation.setToken(token);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        invitationRepository.save(invitation);

        // Contruir link de activación y enviar email
        String activationLink = frontendUrl + "/activate?token=" + token;
        emailService.sendInvitationEmail(user.getEmail(), user.getFullName(), activationLink);
    }

    @Override
    @Transactional
    public void activateAccount(String token, String newPassword, String confirmPassword)
            throws NotFoundException, BusinessRuleException {

        UserInvitationEntity invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token activate not found"));

        if (invitation.isUsed()) {
            throw new BusinessRuleException("This activation link has already been used. Please request a new one from the administrator.");
        }

        if (invitation.isExpired()) {
            throw new BusinessRuleException(
                    "This activation link has expired. Please request a new one from the administrator.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessRuleException("The new password and confirmation do not match.");
        }

        // Activar cuenta del usuario
        UserEntity user = invitation.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setIsActive(true);
        userRepository.save(user);

        // Marcar invitacion como usada
        invitation.setUsedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }
}