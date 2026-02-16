package com.alessandro.congress_management.models.authentication_and_users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity(name = "UserRole")
@Table(name = "user_role")
@Data
@NoArgsConstructor
public class UserRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user_role")
    private Long idUserRole;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "id_role", referencedColumnName = "id_role")
    private RoleEntity role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
