package com.alessandro.congress_management.models.submissions_and_evaluations;

import com.alessandro.congress_management.models.congress_management.CongressEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "CallForPapers")
@Table(name = "call_for_papers")
@Data
@NoArgsConstructor
public class CallForPapersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_call")
    private Long idCall;

    @ManyToOne
    @JoinColumn(name = "id_congress", referencedColumnName = "id_congress")
    private CongressEntity congress;

    @Column(name = "call_name")
    private String callName;

    @Column(name = "description")
    private String description;

    @Column(name = "open_date")
    private Timestamp openDate;

    @Column(name = "close_date")
    private Timestamp closeDate;

    @Column(name = "is_open")
    private Boolean isOpen;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
