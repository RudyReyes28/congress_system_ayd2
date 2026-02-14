package com.alessandro.congress_management.models.rooms_and_activities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "ActivityType")
@Table(name = "activity_type")
@Data
@NoArgsConstructor
public class ActivityTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_activity_type")
    private Long idActivityType;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;
}
