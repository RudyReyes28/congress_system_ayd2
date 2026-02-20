package com.alessandro.congress_management.services.system_configuration;

import com.alessandro.congress_management.dto.system_configuration.SystemConfigurationResponse;
import com.alessandro.congress_management.dto.system_configuration.UpdateConfigValue;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import java.util.List;

public interface SystemConfigurationService {

    List<SystemConfigurationEntity> getAllConfigurations();

    SystemConfigurationEntity getConfigurationByKey(String configKey) throws NotFoundException;

    SystemConfigurationResponse updateConfigurationConfigValue(String configKey, UpdateConfigValue updateConfigValue) throws BusinessRuleException, NotFoundException;

}
