package com.alessandro.congress_management.services.submissions.callforpapers;


import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersDetailsResponse;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersRequest;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;

import java.util.List;

public interface CallForPapersService {

    CallForPapersResponse createCallForPapers(Long congressId, CallForPapersRequest request) throws NotFoundException, BusinessRuleException;

    void deleteCallForPapers(Long callId) throws NotFoundException, BusinessRuleException;

    void closeCallForPapers(Long callId) throws NotFoundException;

    CallForPapersEntity findCallForPapersEntityById(Long callId) throws NotFoundException;

    List<CallForPapersDetailsResponse> getAllCallForPapersByAdminCongress(Long idAdminCongress) throws NotFoundException;

    List<CongressResponse> elegibleCongressesForCallForPapers(Long idAdminCongress) throws NotFoundException;

    //List<CallForPapersResponse> getCallForPapersDetailsByCongressId(Long congressId);

    //Esta vista es para los usuarios que no son admin, para mostrar solo las convocatorias abiertas
    CallForPapersDetailsResponse getCallForPapersOpenByCongressId(Long congressId) throws NotFoundException;

}
