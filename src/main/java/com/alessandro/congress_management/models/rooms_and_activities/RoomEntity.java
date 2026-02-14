package com.alessandro.congress_management.models.rooms_and_activities;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "Room")
@Table(name = "room")
@Data
@NoArgsConstructor
public class RoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_room")
    private Long idRoom;

    @ManyToOne
    @JoinColumn(name = "id_congress", referencedColumnName = "id_congress")
    private CongressEntity congress;

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "room_code")
    private String roomCode;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
