package com.alessandro.congress_management.models.rooms_and_activities;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "Activity")
@Table(name = "activity")
@Data
@NoArgsConstructor
public class ActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_activity")
    private Long idActivity;

    @ManyToOne
    @JoinColumn(name = "id_congress", referencedColumnName = "id_congress")
    private CongressEntity congress;

    @ManyToOne
    @JoinColumn(name = "id_room", referencedColumnName = "id_room")
    private RoomEntity room;

    @ManyToOne
    @JoinColumn(name = "id_activity_type", referencedColumnName = "id_activity_type")
    private ActivityTypeEntity activityType;

    @Column(name = "activity_name")
    private String activityName;

    @Column(name = "description")
    private String description;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
