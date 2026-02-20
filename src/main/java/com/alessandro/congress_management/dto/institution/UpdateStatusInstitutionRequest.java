package com.alessandro.congress_management.dto.institution;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class UpdateStatusInstitutionRequest {
    @NotNull
    boolean active;
}
