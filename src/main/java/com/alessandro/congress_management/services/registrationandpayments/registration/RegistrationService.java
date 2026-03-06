package com.alessandro.congress_management.services.registrationandpayments.registration;


import com.alessandro.congress_management.dto.registrationandpayments.registration.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.util.List;

public interface RegistrationService {

    RegistrationResponse registerForCongress(Long congressId, Long userId) throws NotFoundException, BusinessRuleException;

    List<MyCongressRegistrationDTO> getMyRegistrations(Long userId);

    List<RegisteredUserDTO> getRegistrationsByCongress(Long congressId);

}
