package com.alessandro.congress_management.repositories.system_configuration;

import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfigurationEntity, Integer> {

    Optional<SystemConfigurationEntity> findByConfigKey(String configKey);

}
