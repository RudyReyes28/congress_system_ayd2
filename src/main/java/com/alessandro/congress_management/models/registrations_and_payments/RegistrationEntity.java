package com.alessandro.congress_management.models.registrations_and_payments;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "Registration")
@Table(name = "registration")
@Data
@NoArgsConstructor
public class RegistrationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registration")
    private Long idRegistration;

    @ManyToOne
    @JoinColumn(name = "id_congress", referencedColumnName = "id_congress")
    private CongressEntity congress;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "amount_paid", nullable = false)
    private Double amountPaid;

    @Column(name = "registration_date", nullable = false)
    private Timestamp registrationDate;

    @Column(name = "created_at")
    private Timestamp createdAt;

}
