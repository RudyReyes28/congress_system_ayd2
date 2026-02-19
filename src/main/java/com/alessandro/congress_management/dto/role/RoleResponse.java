package com.alessandro.congress_management.dto.role;

import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import lombok.Value;

@Value
public class RoleResponse {
    Integer idRole;
    String nameRole;
    String description;

    public RoleResponse(RoleEntity roleEntity) {
        this.idRole = roleEntity.getIdRole();
        this.nameRole = roleEntity.getRoleName();
        this.description= roleEntity.getDescription();
    }

}
