package com.alessandro.congress_management.dto.institution;

import lombok.Value;

@Value
public class UpdateInstitutionRequest {
    String institutionName;
    String description;
    String address;
    String contactEmail;
    String contactPhone;
}
