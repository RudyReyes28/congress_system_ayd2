package com.alessandro.congress_management.services.certificate;

import com.alessandro.congress_management.dto.certificate.CertificateResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.FileStorageException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.io.IOException;
import java.util.List;

public interface CertificateService {


    void generateCertificatesForCongress(Long idCongress, Long idAdmin)
            throws NotFoundException, BusinessRuleException, IOException, FileStorageException;

    CertificateResponse generateAttendanceCertificate(Long idCongress, Long idUser)
            throws NotFoundException, BusinessRuleException, IOException;

    CertificateResponse generatePresentationCertificate(Long idCongress, Long idActivity, Long idUser)
            throws NotFoundException, BusinessRuleException, IOException;

    List<CertificateResponse> getCertificatesByUser(Long idUser) throws NotFoundException;

    List<CertificateResponse> getCertificatesByCongress(Long idCongress) throws NotFoundException;
}