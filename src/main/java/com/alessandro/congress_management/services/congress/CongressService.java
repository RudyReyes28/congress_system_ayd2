package com.alessandro.congress_management.services.congress;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.congress.CreateCongressRequest;
import com.alessandro.congress_management.dto.congress.UpdateCongressRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressEntity;

import java.util.List;

public interface CongressService {
    CongressResponse createCongress(CreateCongressRequest request) throws DuplicatedEntityException, BusinessRuleException, NotFoundException;

    CongressResponse updateCongress(Long idCongress, UpdateCongressRequest request) throws BusinessRuleException, NotFoundException;

    //For using the entity in other services
    CongressEntity findCongressEntityById(Long idCongress) throws NotFoundException;

    //Congresses por admin system
    List<CongressResponse> getAllCongresses();

    //Congresses por general public
    List<CongressResponse> getActiveCongresses();

    //Add admin to congress
    void addAdministrator(Long congressId, Long userId) throws NotFoundException, BusinessRuleException;

    //Remove admin from congress
    void removeAdministrator(Long congressId, Long userId) throws NotFoundException, BusinessRuleException;

}
