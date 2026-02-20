package com.alessandro.congress_management.services.system_configuration;

import com.alessandro.congress_management.dto.system_configuration.SystemConfigurationResponse;
import com.alessandro.congress_management.dto.system_configuration.UpdateConfigValue;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import com.alessandro.congress_management.repositories.system_configuration.SystemConfigurationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SystemConfigurationServiceImpl implements SystemConfigurationService{

    private final SystemConfigurationRepository systemConfigurationRepository;

    public SystemConfigurationServiceImpl(SystemConfigurationRepository systemConfigurationRepository) {
        this.systemConfigurationRepository = systemConfigurationRepository;
    }

    @Override
    public List<SystemConfigurationEntity> getAllConfigurations() {
        return systemConfigurationRepository.findAll();

    }

    @Override
    public SystemConfigurationEntity getConfigurationByKey(String configKey) throws NotFoundException {
        return systemConfigurationRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new NotFoundException("Configuration with key " + configKey + " not found"));
    }

    @Override
    public SystemConfigurationResponse updateConfigurationConfigValue(String configKey, UpdateConfigValue updateConfigValue) throws BusinessRuleException, NotFoundException {
        SystemConfigurationEntity config = systemConfigurationRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new NotFoundException("Configuration with key " + configKey + " not found"));

        config.setConfigValue(updateConfigValue.getConfigValue());
        config.setUpdatedAt(LocalDateTime.now());
        SystemConfigurationEntity updatedConfig = systemConfigurationRepository.save(config);
        return SystemConfigurationResponse.fromEntity(updatedConfig);
    }
}
