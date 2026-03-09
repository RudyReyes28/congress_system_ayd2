package com.alessandro.congress_management.repositories.certificate;

import com.alessandro.congress_management.models.certificates.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Repository
public interface CertificateRepository extends JpaRepository<CertificateEntity, Long> {

    int countByUser_IdUserAndCongress_IdCongressAndCertificateType(Long userId, Long congressId, String certificateType);

    boolean existsByUser_IdUserAndCongress_IdCongressAndCertificateType(Long idUser, Long idCongress, String certAttendance);

    boolean existsByUser_IdUserAndActivity_IdActivity(Long idUser, Long idActivity);

    Optional<CertificateEntity> findByUser_IdUserAndCongress_IdCongressAndCertificateType(Long idUser, Long idCongress, String certAttendance);

    Optional<CertificateEntity> findByUser_IdUserAndActivity_IdActivity(Long idUser, Long idActivity);

    List<CertificateEntity> findByUser_IdUser(Long idUser);

    List<CertificateEntity> findByCongress_IdCongress(Long idCongress);
}
