package com.alessandro.congress_management.dto.institution;

import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import lombok.Value;

@Value
public class CreateInstitutionRequest {
    String institutionName;
    String description;
    String address;
    String contactEmail;
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
