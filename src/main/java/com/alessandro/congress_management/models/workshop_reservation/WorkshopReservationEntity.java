package com.alessandro.congress_management.models.workshop_reservation;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "WorkshopReservation")
@Table(name = "workshop_reservation")
@Data
@NoArgsConstructor
public class WorkshopReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation")
    private Long idReservation;

    @ManyToOne
    @JoinColumn(name = "id_activity", referencedColumnName = "id_activity")
    private ActivityEntity activity;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "reserved_at")
    private Timestamp reservedAt;


}
