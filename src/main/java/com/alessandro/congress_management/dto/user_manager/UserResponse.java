package com.alessandro.congress_management.dto.user_manager;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import lombok.Value;

@Value
public class UserResponse {
    Long idUser;
    String username;
    String email;
    String fullName;
    String phoneNumber;
    String organization;
    String identificationNumber;
    String roleName;
    Boolean isActive;

    public UserResponse(UserEntity userEntity) {
         this.idUser = userEntity.getIdUser();
         this.username = userEntity.getUsername();
         this.email = userEntity.getEmail();
         this.fullName = userEntity.getFullName();
         this.phoneNumber = userEntity.getPhoneNumber();
         this.organization = userEntity.getOrganization();
         this.identificationNumber = userEntity.getIdentificationNumber();
         this.roleName = userEntity.getRole().getRoleName();
         this.isActive = userEntity.getIsActive();
    }


}
