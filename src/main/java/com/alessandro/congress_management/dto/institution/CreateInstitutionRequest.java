package com.alessandro.congress_management.dto.institution;

import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class CreateInstitutionRequest {
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

        public InstitutionEntity toEntity() {
            InstitutionEntity institution = new InstitutionEntity();
            institution.setInstitutionName(institutionName);
            institution.setDescription(description);
            institution.setAddress(address);
            institution.setContactEmail(contactEmail);
            institution.setContactPhone(contactPhone);
            institution.setIsActive(true); // Set default active status
            return institution;
        }
}
