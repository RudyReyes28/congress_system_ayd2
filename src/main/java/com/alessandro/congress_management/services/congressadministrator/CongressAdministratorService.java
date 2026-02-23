package com.alessandro.congress_management.services.congressadministrator;

import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressAdministratorEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;

import java.util.List;

public interface CongressAdministratorService {
    CongressAdministratorEntity findCongressAdminEntityById(Long idCongressAdmin) throws NotFoundException;

    CongressAdministratorEntity assignAdministratorToCongress(Long idUser, Long idCongress) throws NotFoundException, BusinessRuleException;

    List<CongressEntity> getCongressesByAdministrator(Long idUser) throws NotFoundException;
}
