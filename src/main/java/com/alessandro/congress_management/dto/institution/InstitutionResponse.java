package com.alessandro.congress_management.dto.institution;

import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import lombok.Value;

@Value
public class InstitutionResponse {
    Long idInstitution;
    String institutionName;
    String description;
    String address;
    String contactEmail;
    String contactPhone;

    public static InstitutionResponse fromEntity(InstitutionEntity institution) {
        return new InstitutionResponse(
                institution.getIdInstitution(),
                institution.getInstitutionName(),
                institution.getDescription(),
                institution.getAddress(),
                institution.getContactEmail(),
                institution.getContactPhone()
        );
    }

}
