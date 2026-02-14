package com.alessandro.congress_management.models.attendance;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "Attendance")
@Table(name = "attendance")
@Data
@NoArgsConstructor
public class AttendanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_attendance")
    private Long idAttendance;

    @ManyToOne
    @JoinColumn(name = "id_activity", referencedColumnName = "id_activity")
    private ActivityEntity activity;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "id_participation_type", referencedColumnName = "id_participation_type")
    private ParticipationTypeEntity participationType;

    @ManyToOne
    @JoinColumn(name = "recorded_by", referencedColumnName = "id_user")
    private UserEntity recordedBy;

    @Column(name = "recorded_at")
    private Timestamp recordedAt;
}
