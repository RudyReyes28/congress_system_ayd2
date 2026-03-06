package com.alessandro.congress_management.models.congress_management;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity(name = "CongressAdministrator")
@Table(name = "congress_administrator")
@Data
@NoArgsConstructor
public class CongressAdministratorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_congress_admin")
    private Long idCongressAdmin;

    @ManyToOne
    @JoinColumn(name = "id_congress", referencedColumnName = "id_congress")
    private CongressEntity congress;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}