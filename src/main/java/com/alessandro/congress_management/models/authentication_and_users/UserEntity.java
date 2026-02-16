package com.alessandro.congress_management.models.authentication_and_users;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity(name = "User")
@Table(name= "user")
@Data
@NoArgsConstructor


public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @Column
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column
    private String organization;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column
    private String username;

    @Column
    private String password;

    @Column(name = "identification_number")
    private String identificationNumber;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "wallet_balance")
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
