package com.alessandro.congress_management.services.submissions.callforpapers;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersDetailsResponse;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersRequest;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressAdministratorEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.repositories.submissions.callforpapers.CallForPapersRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.congressadministrator.CongressAdministratorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CallForPapersServiceImpl implements CallForPapersService{
    private final CallForPapersRepository callForPapersRepository;
    private final CongressService congressService;
    private final SubmissionRepository submissionRepository;
    private final CongressAdministratorService congressAdministratorService;

    public CallForPapersServiceImpl(CallForPapersRepository callForPapersRepository, CongressService congressService, SubmissionRepository submissionRepository, CongressAdministratorService congressAdministratorService) {
        this.callForPapersRepository = callForPapersRepository;
        this.congressService = congressService;
        this.submissionRepository = submissionRepository;
        this.congressAdministratorService = congressAdministratorService;
    }


    @Override
    public CallForPapersResponse createCallForPapers(Long congressId, CallForPapersRequest request) throws NotFoundException, BusinessRuleException {
        //Verificar que la fecha de cierre sea posterior a la fecha de apertura
        if (request.getCloseDate().isBefore(request.getOpenDate())) {
            throw new BusinessRuleException("Close date must be after open date");
        }

        //Obtener el congreso
        CongressEntity congress = congressService.findCongressEntityById(congressId);
        //Verificar que el congrego este activo
        if (!congress.getIsActive()) {
            throw new BusinessRuleException("Congress is not active");
        }

        //Verificar que la fecha de cierre de la convocatoria no sea después de la fecha de finalización del congreso
        if (request.getCloseDate().isAfter(congress.getEndDate().atStartOfDay())) {
            throw new BusinessRuleException("Close date must be before congress end date");
        }

        //Verificar que no exista una convocatoria abierta para el congreso
        if (callForPapersRepository.existsByCongress_IdCongressAndIsOpenTrue(congressId)) {
            throw new BusinessRuleException("There is already an open call for papers for this congress");
        }

        //Crear la convocatoria
        CallForPapersEntity callForPapersEntity =  request.toEntity(congress);

        CallForPapersEntity savedCallForPapers = callForPapersRepository.save(callForPapersEntity);

        return CallForPapersResponse.fromEntity(savedCallForPapers);
    }

    @Override
    public void deleteCallForPapers(Long callId) throws NotFoundException, BusinessRuleException {
        //Verificar que la convocatoria exista
        CallForPapersEntity callForPapersEntity = findCallForPapersEntityById(callId);
        //Verificar que no tenga sumisiones asociadas
        if (submissionRepository.existsByCallForPapers_IdCall(callId)) {
            throw new BusinessRuleException("Cannot delete call for papers with submissions");
        }
        //Eliminar la convocatoria
        callForPapersRepository.delete(callForPapersEntity);


    }

    @Override
    public void closeCallForPapers(Long callId) throws NotFoundException {
        //Verificar que la convocatoria exista
        CallForPapersEntity callForPapersEntity = findCallForPapersEntityById(callId);
        //Cerrar la convocatoria
        callForPapersEntity.setIsOpen(false);
        callForPapersRepository.save(callForPapersEntity);

    }

    @Override
    public CallForPapersEntity findCallForPapersEntityById(Long callId) throws NotFoundException {
        return callForPapersRepository.findById(callId)
                .orElseThrow(() -> new NotFoundException("Call for papers not found "));
    }

    @Override
    public List<CallForPapersDetailsResponse> getAllCallForPapersByAdminCongress(Long idAdminCongress) throws NotFoundException {
        //Obtener los congresos administrados por el admin
        List<CongressResponse> congresses = congressAdministratorService.getCongressesByAdministrator(idAdminCongress);
        //Obtener las convocatorias de cada congreso
        return congresses.stream()
                .flatMap(congress -> callForPapersRepository.findByCongress_IdCongress(congress.getIdCongress()).stream())
                .map(CallForPapersDetailsResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CongressResponse> elegibleCongressesForCallForPapers(Long idAdminCongress) throws NotFoundException {
        //Obtener los congresos administrados por el admin
        List<CongressResponse> congresses = congressAdministratorService.getCongressesByAdministrator(idAdminCongress);
        //Filtrar los congresos que no tengan convocatorias abiertas y que esten activos
        return congresses.stream()
                .filter(congress -> !callForPapersRepository.existsByCongress_IdCongressAndIsOpenTrue(congress.getIdCongress()) && congress.isActive())
                .toList();
    }

    @Override
    public CallForPapersDetailsResponse getCallForPapersOpenByCongressId(Long congressId) throws NotFoundException {
        //Obtener el congreso
        CongressEntity congress = congressService.findCongressEntityById(congressId);

        //Obtener la convocatoria abierta del congreso
        CallForPapersEntity callForPapersEntity = callForPapersRepository.findByCongress_IdCongressAndIsOpenTrue(congressId)
                .orElseThrow(() -> new NotFoundException("No open call for papers found for this congress"));

        return CallForPapersDetailsResponse.fromEntity(callForPapersEntity);
    }
}
