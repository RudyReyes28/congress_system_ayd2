package com.alessandro.congress_management.services.certificate;

import com.alessandro.congress_management.dto.certificate.CertificateResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.FileStorageException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.certificates.CertificateEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activitypresenter.ActivityPresenterRepository;
import com.alessandro.congress_management.repositories.attendance.AttendanceRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.certificate.CertificateRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.services.storage.FileStorageService;
import com.alessandro.congress_management.utils.ByteArrayMultipartFile;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl implements CertificateService {

    private static final String CERT_ATTENDANCE  = "ATTENDANCE";
    private static final String CERT_PRESENTATION = "PRESENTATION";
    private static final String STORAGE_FOLDER = "certificates";
    private static final String PDF_CONTENT_TYPE  = "application/pdf";
    private static final int    MIN_ATTENDANCES = 3;
    private static final String PART_ATTENDEE = "ATTENDEE";
    private static final String PART_PRESENTER = "PRESENTER";
    private static final String PART_INVITED = "INVITED_SPEAKER";

    private final CertificateRepository certificateRepository;
    private final CongressRepository congressRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final AttendanceRepository attendanceRepository;
    private final ActivityPresenterRepository activityPresenterRepository;
    private final CongressAdministratorRepository congressAdministratorRepository;
    private final FileStorageService fileStorageService;
    private final CertificatePdfBuilder pdfBuilder;

    public CertificateServiceImpl(CertificateRepository certificateRepository, CongressRepository congressRepository, UserRepository userRepository, ActivityRepository activityRepository, AttendanceRepository attendanceRepository, ActivityPresenterRepository activityPresenterRepository, CongressAdministratorRepository congressAdministratorRepository, FileStorageService fileStorageService, CertificatePdfBuilder pdfBuilder) {
        this.certificateRepository = certificateRepository;
        this.congressRepository = congressRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.attendanceRepository = attendanceRepository;
        this.activityPresenterRepository = activityPresenterRepository;
        this.congressAdministratorRepository = congressAdministratorRepository;
        this.fileStorageService = fileStorageService;
        this.pdfBuilder = pdfBuilder;
    }


    //-------- GENERATE CERTIFICATES FOR CONGRESS  ---------------------------------------

    @Async
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateCertificatesForCongress(Long idCongress, Long idAdmin)
            throws NotFoundException, BusinessRuleException, IOException, FileStorageException {

        CongressEntity congress = findCongress(idCongress);

        if (!congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(idAdmin, idCongress)) {
            throw new BusinessRuleException("User is not an administrator of this congress.");
        }

        List<AttendanceEntity> allAttendances =
                attendanceRepository.findByActivity_Congress_IdCongress(idCongress);

        String dateRange = formatCongressDateRange(congress);
        List<CertificateResponse> generated = new ArrayList<>();

        // 1. ATTENDEE: Un certificado por usuario si tiene al menos 3 asistencias. IdActivity = null. Skip si ya existe para user+congress+type
        Map<UserEntity, Long> attendeeCounts = allAttendances.stream()
                .filter(a -> PART_ATTENDEE.equals(a.getParticipationType().getTypeName()))
                .collect(Collectors.groupingBy(AttendanceEntity::getUser, Collectors.counting()));

        for (Map.Entry<UserEntity, Long> entry : attendeeCounts.entrySet()) {
            UserEntity user = entry.getKey();
            long  count = entry.getValue();

            if (count < MIN_ATTENDANCES) continue;

            boolean alreadyIssued = certificateRepository
                    .existsByUser_IdUserAndCongress_IdCongressAndCertificateType(
                            user.getIdUser(), idCongress, CERT_ATTENDANCE);
            if (alreadyIssued) continue;

            byte[] pdf = pdfBuilder.buildAttendanceCertificate(
                    user.getFullName(), congress.getCongressName(),
                    congress.getDescription(), dateRange);

            String url = uploadPdf(pdf, "attendance_" + idCongress + "_" + user.getIdUser());
            generated.add(CertificateResponse.fromEntity(
                    certificateRepository.save(buildEntity(congress, user, CERT_ATTENDANCE, null, url))));
        }

        // 2. PRESENTER / INVITED_SPEAKER: Un certificado por actividad presentada. Skip si ya existe para user+activity
        List<AttendanceEntity> presenterAttendances = allAttendances.stream()
                .filter(a -> PART_PRESENTER.equals(a.getParticipationType().getTypeName())
                        || PART_INVITED.equals(a.getParticipationType().getTypeName()))
                .toList();

        for (AttendanceEntity attendance : presenterAttendances) {
            UserEntity     user     = attendance.getUser();
            ActivityEntity activity = attendance.getActivity();

            boolean alreadyIssued = certificateRepository
                    .existsByUser_IdUserAndActivity_IdActivity(
                            user.getIdUser(), activity.getIdActivity());
            if (alreadyIssued) continue;

            byte[] pdf = pdfBuilder.buildPresentationCertificate(
                    user.getFullName(), congress.getCongressName(), congress.getDescription(),
                    activity.getActivityName(), activity.getDescription(), dateRange);

            String url = uploadPdf(pdf, "presentation_" + activity.getIdActivity() + "_" + user.getIdUser());
            generated.add(CertificateResponse.fromEntity(
                    certificateRepository.save(buildEntity(congress, user, CERT_PRESENTATION, activity, url))));
        }

        //return generated;
    }

    // ------- GENERATE ATTENDANCE CERTIFICATE  ---------------------------------------

    @Override
    public CertificateResponse generateAttendanceCertificate(Long idCongress, Long idUser)
            throws NotFoundException, BusinessRuleException, IOException {

        CongressEntity congress = findCongress(idCongress);
        UserEntity user = findUser(idUser);

        return certificateRepository
                .findByUser_IdUserAndCongress_IdCongressAndCertificateType(idUser, idCongress, CERT_ATTENDANCE)
                .map(CertificateResponse::fromEntity)
                .orElseGet(() -> {
                    try { return doGenerateAttendance(congress, user); }
                    catch (IOException | BusinessRuleException | FileStorageException e) { throw new RuntimeException("PDF generation failed", e); }
                });
    }

    private CertificateResponse doGenerateAttendance(CongressEntity congress, UserEntity user)
            throws IOException, BusinessRuleException, FileStorageException {
        long count = attendanceRepository.countByUser_IdUserAndActivity_Congress_IdCongress(
                user.getIdUser(), congress.getIdCongress());
        if (count < MIN_ATTENDANCES) {
            throw new BusinessRuleException(
                    "User has " + count + " attendance(s). Minimum required: " + MIN_ATTENDANCES + ".");
        }
        String dateRange = formatCongressDateRange(congress);
        byte[] pdf = pdfBuilder.buildAttendanceCertificate(
                user.getFullName(), congress.getCongressName(), congress.getDescription(), dateRange);
        String url = uploadPdf(pdf, "attendance_" + congress.getIdCongress() + "_" + user.getIdUser());
        return CertificateResponse.fromEntity(
                certificateRepository.save(buildEntity(congress, user, CERT_ATTENDANCE, null, url)));
    }

    //-------- GENERATE PRESENTATION CERTIFICATE  ---------------------------------------

    @Override
    public CertificateResponse generatePresentationCertificate(Long idCongress, Long idActivity, Long idUser)
            throws NotFoundException, BusinessRuleException, IOException {

        CongressEntity congress = findCongress(idCongress);
        UserEntity     user     = findUser(idUser);
        ActivityEntity activity = findActivity(idActivity);

        if (!activity.getCongress().getIdCongress().equals(idCongress)) {
            throw new BusinessRuleException("Activity does not belong to the specified congress.");
        }

        return certificateRepository
                .findByUser_IdUserAndActivity_IdActivity(idUser, idActivity)
                .map(CertificateResponse::fromEntity)
                .orElseGet(() -> {
                    try { return doGeneratePresentation(congress, user, activity); }
                    catch (IOException | BusinessRuleException | FileStorageException e) { throw new RuntimeException("PDF generation failed", e); }
                });
    }

    private CertificateResponse doGeneratePresentation(CongressEntity congress,
                                                       UserEntity user,
                                                       ActivityEntity activity) throws IOException, BusinessRuleException, FileStorageException {
        if (!activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(
                activity.getIdActivity(), user.getIdUser())) {
            throw new BusinessRuleException("User was not a presenter for this activity.");
        }
        String dateRange = formatCongressDateRange(congress);
        byte[] pdf = pdfBuilder.buildPresentationCertificate(
                user.getFullName(), congress.getCongressName(), congress.getDescription(),
                activity.getActivityName(), activity.getDescription(), dateRange);
        String url = uploadPdf(pdf, "presentation_" + activity.getIdActivity() + "_" + user.getIdUser());
        return CertificateResponse.fromEntity(
                certificateRepository.save(buildEntity(congress, user, CERT_PRESENTATION, activity, url)));
    }

    // ------- GET CERTIFICATES  ---------------------------------------

    @Override
    public List<CertificateResponse> getCertificatesByUser(Long idUser) throws NotFoundException {
        findUser(idUser);
        return certificateRepository.findByUser_IdUser(idUser)
                .stream().map(CertificateResponse::fromEntity).toList();
    }

    @Override
    public List<CertificateResponse> getCertificatesByCongress(Long idCongress) throws NotFoundException {
        findCongress(idCongress);
        return certificateRepository.findByCongress_IdCongress(idCongress)
                .stream().map(CertificateResponse::fromEntity).toList();
    }

    // ------ HELPERS  ---------------------------------------

    private CongressEntity findCongress(Long id) throws NotFoundException {
        return congressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Congress not found"));
    }

    private UserEntity findUser(Long id) throws NotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ActivityEntity findActivity(Long id) throws NotFoundException {
        return activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));
    }

    private CertificateEntity buildEntity(CongressEntity congress, UserEntity user,
                                                                                                 String type, ActivityEntity activity, String url) {
        CertificateEntity e = new CertificateEntity();
        e.setCongress(congress);
        e.setUser(user);
        e.setCertificateType(type);
        e.setActivity(activity);
        e.setCertificateUrl(url);
        return e;
    }

    private String formatCongressDateRange(CongressEntity congress) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        return congress.getStartDate().format(fmt) + " – " + congress.getEndDate().format(fmt);
    }

    private String uploadPdf(byte[] pdfBytes, String fileName) throws IOException, FileStorageException {
        MultipartFile file =  new ByteArrayMultipartFile(
                pdfBytes, fileName, fileName + ".pdf", PDF_CONTENT_TYPE);
        return fileStorageService.uploadFile(file, STORAGE_FOLDER);
    }
}