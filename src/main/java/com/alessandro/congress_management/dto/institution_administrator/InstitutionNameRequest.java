package com.alessandro.congress_management.dto.institution_administrator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class InstitutionNameRequest {
    @NotBlank(message = "Institution name must not be blank")
    String institutionName;
}
