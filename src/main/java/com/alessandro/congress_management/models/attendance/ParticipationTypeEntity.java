package com.alessandro.congress_management.models.attendance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "ParticipationType")
@Table(name = "participation_type")
@Data
@NoArgsConstructor
public class ParticipationTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participation_type")
    private Long idParticipationType;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;
}
