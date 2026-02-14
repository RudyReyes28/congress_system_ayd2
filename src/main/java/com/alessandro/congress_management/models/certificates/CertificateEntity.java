package com.alessandro.congress_management.models.certificates;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "Certificate")
@Table(name = "certificate")
@Data
@NoArgsConstructor
public class CertificateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificate")
    private Long idCertificate;

    @ManyToOne
    @JoinColumn(name = "id_congress", referencedColumnName = "id_congress")
    private CongressEntity congress;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private UserEntity user;

    @Column(name = "certificate_type", nullable = false)
    private String certificateType;

    @ManyToOne
    @JoinColumn(name = "id_activity", referencedColumnName = "id_activity")
    private ActivityEntity activity;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "generated_at")
    private Timestamp generatedAt;
    
}
