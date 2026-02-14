package com.alessandro.congress_management.models.registrations_and_payments;


import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "WalletTransaction")
@Table(name = "wallet_transaction")
@Data
@NoArgsConstructor
public class WalletTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Long idTransaction;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "related_registration_id", referencedColumnName = "id_registration")
    private RegistrationEntity relatedRegistration;

    @Column(name = "transaction_date", nullable = false)
    private Timestamp transactionDate;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
