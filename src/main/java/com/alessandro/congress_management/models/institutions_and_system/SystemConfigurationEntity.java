package com.alessandro.congress_management.models.institutions_and_system;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "SystemConfiguration")
@Table(name = "system_configuration")
@Data
@NoArgsConstructor
public class SystemConfigurationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_config")
    private Long idConfig;

    @Column(name = "config_key")
    private String configKey;

    @Column(name = "config_value")
    private String configValue;

    @Column(name = "description")
    private String description;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
