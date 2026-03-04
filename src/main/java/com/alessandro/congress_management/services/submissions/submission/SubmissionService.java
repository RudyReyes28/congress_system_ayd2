package com.alessandro.congress_management.services.submissions.submission;

import com.alessandro.congress_management.dto.submissions.submission.SubmissionDetails;
import com.alessandro.congress_management.dto.submissions.submission.SubmissionRequest;
import com.alessandro.congress_management.dto.submissions.submission.SubmissionResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.FileStorageException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {

    SubmissionResponse submit(Long idCall, Long idUser, SubmissionRequest request, MultipartFile file)
            throws NotFoundException, BusinessRuleException, FileStorageException;


    SubmissionResponse resubmit(Long idSubmission, Long idUser, SubmissionRequest request, MultipartFile file)
            throws NotFoundException, BusinessRuleException, FileStorageException;


    List<SubmissionResponse> getSubmissionsByCall(Long idCall) throws NotFoundException;


    List<SubmissionDetails> getSubmissionsByUser(Long idUser) throws NotFoundException;

}