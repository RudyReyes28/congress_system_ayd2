package com.alessandro.congress_management.models.authentication_and_users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "Role")
@Table(name = "role")
@Data
@NoArgsConstructor
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long idRole;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "description")
    private String description;

}
