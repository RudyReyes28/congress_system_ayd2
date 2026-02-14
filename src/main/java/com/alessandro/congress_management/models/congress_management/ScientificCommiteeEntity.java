package com.alessandro.congress_management.models.congress_management;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "ScientificCommitee")
@Table(name = "scientific_commitee")
@Data
@NoArgsConstructor
public class ScientificCommiteeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_committee_member")
    private Long idCommitteeMember;

    @ManyToOne
    @JoinColumn(name = "id_congress", referencedColumnName = "id_congress")
    private CongressEntity congress;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "assigned_at")
    private Timestamp assignedAt;
}
