package com.alessandro.congress_management.dto.institution;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class UpdateInstitutionRequest {
    @NotBlank(message = "Institution name cannot be blank")
    String institutionName;
    @NotBlank(message = "Description cannot be blank")
    String description;
    @NotBlank(message = "Address cannot be blank")
    String address;
    @NotBlank(message = "Contact email cannot be blank")
    @Email(message = "Email should be valid")
    String contactEmail;
    @NotBlank(message = "Contact phone cannot be blank")
    String contactPhone;
}
