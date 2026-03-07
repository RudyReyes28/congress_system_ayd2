package com.alessandro.congress_management.services.invitation;

import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

public interface UserInvitationService {

    void sendInvitation(Long idUser) throws NotFoundException;

    void activateAccount(String token, String newPassword, String confirmPassword)
            throws NotFoundException, BusinessRuleException;
}