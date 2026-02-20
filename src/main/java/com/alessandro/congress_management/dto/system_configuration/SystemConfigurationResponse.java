package com.alessandro.congress_management.dto.system_configuration;

import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class SystemConfigurationResponse {
    Integer idConfig;
    String configKey;
    String configValue;
    String description;
    LocalDateTime updatedAt;

    public static SystemConfigurationResponse fromEntity(SystemConfigurationEntity entity) {
        return new SystemConfigurationResponse(
                entity.getIdConfig(),
                entity.getConfigKey(),
                entity.getConfigValue(),
                entity.getDescription(),
                entity.getUpdatedAt()
        );
    }
}
