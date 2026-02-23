package com.alessandro.congress_management.dto.congress;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.congress_management.InstitutionAdministratorEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import jakarta.validation.constraints.*;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class CreateCongressRequest {

    @NotNull(message = "Congress Manager cannot be null")
    Long idCongressManager;

    @NotBlank(message = "Congress name cannot be blank")
    String congressName;

    @NotBlank(message = "Description cannot be blank")
    String description;

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be in the present or future")
    LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @FutureOrPresent(message = "End date must be in the present or future")
    LocalDate endDate;

    @NotBlank(message = "Location cannot be blank")
    String location;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "35.0", inclusive = true, message = "Price must be at least 35")
    BigDecimal price;

    public CongressEntity toEntity(InstitutionEntity institution) {
        CongressEntity congress = new CongressEntity();
        congress.setInstitution(institution);
        congress.setCongressName(this.congressName);
        congress.setDescription(this.description);
        congress.setStartDate(this.startDate);
        congress.setEndDate(this.endDate);
        congress.setLocation(this.location);
        congress.setPrice(this.price);
        return congress;
    }
}
