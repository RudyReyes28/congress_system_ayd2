package com.alessandro.congress_management.dto.certificate;

import com.alessandro.congress_management.models.certificates.CertificateEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CertificateResponse {
    Long idCertificate;
    Long idCongress;
    String congressName;
    Long idUser;
    String userName;
    String certificateType;
    Long idActivity;
    String activityName;
    String certificateUrl;
    LocalDateTime generatedAt;

    public static CertificateResponse fromEntity(CertificateEntity entity) {
        return new CertificateResponse(
                entity.getIdCertificate(),
                entity.getCongress().getIdCongress(),
                entity.getCongress().getCongressName(),
                entity.getUser().getIdUser(),
                entity.getUser().getFullName(),
                entity.getCertificateType(),
                entity.getActivity() != null ? entity.getActivity().getIdActivity() : null,
                entity.getActivity() != null ? entity.getActivity().getActivityName() : null,
                entity.getCertificateUrl(),
                entity.getGeneratedAt()
        );
    }
}