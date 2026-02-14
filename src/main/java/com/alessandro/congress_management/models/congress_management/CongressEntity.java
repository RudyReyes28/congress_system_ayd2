package com.alessandro.congress_management.models.congress_management;

import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Entity(name = "Congress")
@Table(name = "congress")
@Data
@NoArgsConstructor
public class CongressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_congress")
    private Long idCongress;

    @ManyToOne
    @JoinColumn(name = "id_institution", referencedColumnName = "id_institution")
    private InstitutionEntity institution;

    @Column(name = "congress_name")
    private String congressName;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "location")
    private String location;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
