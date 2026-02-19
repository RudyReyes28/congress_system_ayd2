package com.alessandro.congress_management.models.congress_management;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "InstitutionAdministrator")
@Table(name = "institution_administrator")
@Data
@NoArgsConstructor
public class InstitutionAdministratorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_institution_admin")
    private Long idInstitutionAdmin;

    @ManyToOne
    @JoinColumn(name = "id_institution", referencedColumnName = "id_institution")
    private InstitutionEntity institution;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
