package com.alessandro.congress_management.dto.activitypresenter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class AssignInviteUserActivityRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email;
    @NotBlank(message = "Full name cannot be blank")
    String fullName;
    @NotBlank(message = "Phone number cannot be blank")
    String phoneNumber;
    @NotBlank(message = "Organization cannot be blank")
    String organization;
    @NotBlank(message = "Username cannot be blank")
    String username;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;
    @NotBlank(message = "Identification number cannot be blank")
    String identificationNumber;

}
