package com.alessandro.congress_management.dto.system_configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Value;

@Value
public class UpdateConfigValue {
    @NotBlank(message = "Config value cannot be blank")
    @Pattern(regexp = "^(100|[1-9]?[0-9])$", message = "Config value must be a number between 0 and 100")
    String configValue; //de 0 a 100
}
