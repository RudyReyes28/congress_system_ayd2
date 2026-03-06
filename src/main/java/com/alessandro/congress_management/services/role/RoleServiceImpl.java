package com.alessandro.congress_management.services.role;

import com.alessandro.congress_management.dto.role.RoleResponse;
import com.alessandro.congress_management.repositories.authenticate.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;


    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::new)
                .toList();
    }
}
