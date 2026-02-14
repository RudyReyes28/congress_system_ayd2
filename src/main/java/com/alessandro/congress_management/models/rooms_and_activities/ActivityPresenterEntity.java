package com.alessandro.congress_management.models.rooms_and_activities;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "ActivityPresenter")
@Table(name = "activity_presenter")
@Data
@NoArgsConstructor
public class ActivityPresenterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_activity_presenter")
    private Long idActivityPresenter;

    @ManyToOne
    @JoinColumn(name = "id_activity", referencedColumnName = "id_activity")
    private ActivityEntity activity;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "is_invited_speaker")
    private Boolean isInvitedSpeaker;

    @Column(name = "created_at")
    private Timestamp createdAt;

}
