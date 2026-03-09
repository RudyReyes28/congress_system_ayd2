package com.alessandro.congress_management.dto.congress;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import lombok.Value;

import java.time.LocalDate;

@Value
public class CongressResponse {
    Long idCongress;
    String institutionName;
    String congressName;
    String description;
    String startDate;
    String endDate;
    String location;
    String price;
    boolean active;

    public static CongressResponse fromEntity(CongressEntity congress) {
        return new CongressResponse(
                congress.getIdCongress(),
                congress.getInstitution().getInstitutionName(),
                congress.getCongressName(),
                congress.getDescription(),
                congress.getStartDate().toString(),
                congress.getEndDate().toString(),
                congress.getLocation(),
                congress.getPrice().toString(),
                congress.getIsActive()
        );
    }
}
