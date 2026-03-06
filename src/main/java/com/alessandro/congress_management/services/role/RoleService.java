package com.alessandro.congress_management.services.role;

import com.alessandro.congress_management.dto.role.RoleResponse;

import java.util.List;

public interface RoleService {

    //Get roles
    List<RoleResponse> getAllRoles();
}
