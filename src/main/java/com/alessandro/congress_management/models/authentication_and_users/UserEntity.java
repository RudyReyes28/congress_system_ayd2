package com.alessandro.congress_management.models.authentication_and_users;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

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
    private String username;

    @Column
    private String password;

    @Column(name = "identification_number")
    private String identificationNumber;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "wallet_balance")
    private BigDecimal walletBalance;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
