package com.alessandro.congress_management.services.submissions.submission;

import com.alessandro.congress_management.dto.submissions.submission.SubmissionRequest;
import com.alessandro.congress_management.dto.submissions.submission.SubmissionResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.FileStorageException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.SubmissionStatusEntity;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.submissions.callforpapers.CallForPapersRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionStatusRepository;
import com.alessandro.congress_management.services.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService{
    private static final String STORAGE_FOLDER = "submissions";
    private static final int STATUS_PENDING  = 1;
    private static final int STATUS_REJECTED = 3;

    private final SubmissionRepository submissionRepository;
    private final CallForPapersRepository callForPapersRepository;
    private final UserRepository userRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final SubmissionStatusRepository submissionStatusRepository;
    private final FileStorageService fileStorageService;
    private final RegistrationRepository registrationRepository;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository, CallForPapersRepository callForPapersRepository, UserRepository userRepository, ActivityTypeRepository activityTypeRepository, SubmissionStatusRepository submissionStatusRepository, FileStorageService fileStorageService, RegistrationRepository registrationRepository) {
        this.submissionRepository = submissionRepository;
        this.callForPapersRepository = callForPapersRepository;
        this.userRepository = userRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.submissionStatusRepository = submissionStatusRepository;
        this.fileStorageService = fileStorageService;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public SubmissionResponse submit(Long idCall, Long idUser, SubmissionRequest request, MultipartFile file) throws NotFoundException, BusinessRuleException, FileStorageException {
        CallForPapersEntity call = findOpenCall(idCall);
        UserEntity user          = findUser(idUser);
        ActivityTypeEntity type  = findActivityType(request.getIdActivityType());
        SubmissionStatusEntity pending = findStatus(STATUS_PENDING);

        // Verificar que el usuario este registrado en el congreso asociado a la convocatoria
        if (!registrationRepository.existsByUser_IdUserAndCongress_IdCongress(idUser, call.getCongress().getIdCongress())) {
            throw new BusinessRuleException("You must be registered for the congress to submit a paper.");
        }

        // Subir el archivo si esta presente y obtener la URL
        String fileUrl = uploadIfPresent(file);


        SubmissionEntity submission = request.toEntity(call, user, type, pending, fileUrl);
        SubmissionEntity savedSubmission = submissionRepository.save(submission);
        return SubmissionResponse.fromEntity(savedSubmission);
    }

    @Override
    public SubmissionResponse resubmit(Long idSubmission, Long idUser, SubmissionRequest request, MultipartFile file) throws NotFoundException, BusinessRuleException, FileStorageException {
        SubmissionEntity existing = findSubmission(idSubmission);

        validateOwnership(existing, idUser);
        validateIsRejected(existing);
        validateCallIsOpen(existing.getCallForPapers());

        ActivityTypeEntity type    = findActivityType(request.getIdActivityType());
        SubmissionStatusEntity pending = findStatus(STATUS_PENDING);

        // Delete the old file from S3 before uploading the new one
        deleteFileIfPresent(existing.getFileUrl());
        String newFileUrl = uploadIfPresent(file);

        existing.setActivityType(type);
        existing.setSubmissionTitle(request.getSubmissionTitle());
        existing.setAbstractText(request.getAbstrakt());
        existing.setFileUrl(newFileUrl);
        existing.setSubmissionStatus(pending);

        SubmissionEntity updatedSubmission = submissionRepository.save(existing);
        return SubmissionResponse.fromEntity(updatedSubmission);
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByCall(Long idCall) throws NotFoundException {
        if (!callForPapersRepository.existsById(idCall)) {
            throw new NotFoundException("Call for papers not found");
        }
        List<SubmissionEntity> submissions = submissionRepository.findByCallForPapers_IdCall(idCall);
        return submissions.stream()
                .map(SubmissionResponse::fromEntity)
                .toList();
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByUser(Long idUser) throws NotFoundException {
        if (!userRepository.existsById(idUser)) {
            throw new NotFoundException("User not found");
        }

        List<SubmissionEntity> submissions = submissionRepository.findByUser_IdUser(idUser);
        return submissions.stream()
                .map(SubmissionResponse::fromEntity)
                .toList();
    }

    //  S3 helpers

    private String uploadIfPresent(MultipartFile file) throws FileStorageException {
        if (file == null || file.isEmpty()) return null;
        return fileStorageService.uploadFile(file, STORAGE_FOLDER);
    }

    private void deleteFileIfPresent(String fileUrl) throws FileStorageException {
        if (fileUrl != null && !fileUrl.isBlank()) {
            fileStorageService.deleteFile(fileUrl);
        }
    }

    //  Finders

    private CallForPapersEntity findOpenCall(Long idCall) throws NotFoundException, BusinessRuleException {
        CallForPapersEntity call = callForPapersRepository.findById(idCall)
                .orElseThrow(() -> new NotFoundException("Call for papers not found"));
        validateCallIsOpen(call);
        return call;
    }

    private UserEntity findUser(Long idUser) throws NotFoundException {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ActivityTypeEntity findActivityType(Integer idActivityType) throws NotFoundException {
        return activityTypeRepository.findById(idActivityType)
                .orElseThrow(() -> new NotFoundException("Activity type not found"));
    }

    private SubmissionStatusEntity findStatus(int idStatus) throws NotFoundException {
        return submissionStatusRepository.findById(idStatus)
                .orElseThrow(() -> new NotFoundException("Submission status not found"));
    }

    private SubmissionEntity findSubmission(Long idSubmission) throws NotFoundException {
        return submissionRepository.findById(idSubmission)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
    }

    //  Business rule validations

    private void validateCallIsOpen(CallForPapersEntity call) throws BusinessRuleException {
        if (!call.getIsOpen()) {
            throw new BusinessRuleException("The call for papers is closed. Submissions are not allowed.");
        }
    }

    private void validateOwnership(SubmissionEntity submission, Long idUser) throws BusinessRuleException {
        if (!submission.getUser().getIdUser().equals(idUser)) {
            throw new BusinessRuleException("You are not the author of this submission.");
        }
    }

    private void validateIsRejected(SubmissionEntity submission) throws BusinessRuleException {
        if (submission.getSubmissionStatus().getIdStatus() != STATUS_REJECTED) {
            throw new BusinessRuleException("Only rejected submissions can be resubmitted.");
        }
    }
}
