package com.alessandro.congress_management.models.authentication_and_users;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "RefreshToken")
@Table(name = "refresh_token")
@Data
@NoArgsConstructor
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long idToken;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "token")
    private String token;

    @Column(name = "expiry_date")
    private Timestamp expiryDate;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
