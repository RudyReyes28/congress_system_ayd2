package com.alessandro.congress_management.dto.scientificcommitee;

import com.alessandro.congress_management.models.congress_management.ScientificCommiteeEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ScientificCommiteeResponse {
    Long idUser;
    String username;
    String email;
    String fullName;
    String phoneNumber;
    String organization;
    String identificationNumber;
    String congressInstitutionName;
    String congressName;
    LocalDateTime assignedAt;

    public static ScientificCommiteeResponse fromEntity (ScientificCommiteeEntity committeeMember) {
        return new ScientificCommiteeResponse(
                committeeMember.getUser().getIdUser(),
                committeeMember.getUser().getUsername(),
                committeeMember.getUser().getEmail(),
                committeeMember.getUser().getFullName(),
                committeeMember.getUser().getPhoneNumber(),
                committeeMember.getUser().getOrganization(),
                committeeMember.getUser().getIdentificationNumber(),
                committeeMember.getCongress().getInstitution().getInstitutionName(),
                committeeMember.getCongress().getCongressName(),
                committeeMember.getAssignedAt()
        );
    }
}
