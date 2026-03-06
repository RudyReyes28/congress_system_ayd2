package com.alessandro.congress_management.services.submissions.callforpapers;

import com.alessandro.congress_management.dto.congress.CongressResponse;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersDetailsResponse;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersRequest;
import com.alessandro.congress_management.dto.submissions.callforpapers.CallForPapersResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.models.submissions_and_evaluations.CallForPapersEntity;
import com.alessandro.congress_management.repositories.submissions.callforpapers.CallForPapersRepository;
import com.alessandro.congress_management.repositories.submissions.submission.SubmissionRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.congressadministrator.CongressAdministratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CallForPapersServiceImplTest {


    private static final Long   ID_CALL          = 1L;
    private static final Long   ID_CONGRESS      = 10L;
    private static final Long   ID_ADMIN_CONGRESS = 5L;

    private static final String CALL_NAME        = "Call for Papers 2026";
    private static final String DESCRIPTION      = "Submit your papers here";
    private static final String CONGRESS_NAME    = "Tech Congress 2026";

    private static final LocalDateTime OPEN_DATE  = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime CLOSE_DATE = LocalDateTime.now().plusDays(30);

    // End date of the congress — must be after CLOSE_DATE
    private static final LocalDate CONGRESS_END_DATE = LocalDate.now().plusDays(60);


    @Mock private CallForPapersRepository     callForPapersRepository;
    @Mock private CongressService             congressService;
    @Mock private SubmissionRepository        submissionRepository;
    @Mock private CongressAdministratorService congressAdministratorService;

    @InjectMocks
    private CallForPapersServiceImpl callForPapersService;

    //-------------- TESTS FOR CREATE CALL FOR PAPERS --------------
    @Test
    void testCreateCallForPapers() throws Exception {
        // Arrange
        CallForPapersRequest request = new CallForPapersRequest(CALL_NAME, DESCRIPTION, OPEN_DATE, CLOSE_DATE);
        ArgumentCaptor<CallForPapersEntity> captor = ArgumentCaptor.forClass(CallForPapersEntity.class);

        when(congressService.findCongressEntityById(ID_CONGRESS)).thenReturn(activeCongress());
        when(callForPapersRepository.existsByCongress_IdCongressAndIsOpenTrue(ID_CONGRESS)).thenReturn(false);
        when(callForPapersRepository.save(any())).thenAnswer(inv -> savedCall(inv.getArgument(0)));

        // Act
        CallForPapersResponse result = callForPapersService.createCallForPapers(ID_CONGRESS, request);

        // Assert
        assertAll(
                () -> verify(callForPapersRepository).save(captor.capture()),
                () -> assertEquals(CALL_NAME,    captor.getValue().getCallName()),
                () -> assertEquals(DESCRIPTION,  captor.getValue().getDescription()),
                () -> assertEquals(OPEN_DATE,    captor.getValue().getOpenDate()),
                () -> assertEquals(CLOSE_DATE,   captor.getValue().getCloseDate()),
                () -> assertEquals(CALL_NAME,    result.getCallName()),
                () -> assertEquals(OPEN_DATE,    result.getOpenDate()),
                () -> assertEquals(CLOSE_DATE,   result.getCloseDate()),
                () -> assertTrue(result.getIsOpen())
        );
    }

    @Test
    void testCreateCallForPapersWhenCloseDateIsBeforeOpenDate() {
        // Arrange
        LocalDateTime openDate  = LocalDateTime.now().plusDays(10);
        LocalDateTime closeDate = LocalDateTime.now().plusDays(5); // before open
        CallForPapersRequest request = new CallForPapersRequest(CALL_NAME, DESCRIPTION, openDate, closeDate);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> callForPapersService.createCallForPapers(ID_CONGRESS, request));

        verifyNoInteractions(congressService, callForPapersRepository);
    }

    @Test
    void testCreateCallForPapersWhenCongressNotFound() throws NotFoundException {
        // Arrange
        CallForPapersRequest request = new CallForPapersRequest(CALL_NAME, DESCRIPTION, OPEN_DATE, CLOSE_DATE);
        when(congressService.findCongressEntityById(ID_CONGRESS)).thenThrow(new NotFoundException("Congress not found"));

        // Assert
        assertThrows(NotFoundException.class,
                () -> callForPapersService.createCallForPapers(ID_CONGRESS, request));
    }

    @Test
    void testCreateCallForPapersWhenCongressIsInactive() throws NotFoundException {
        // Arrange
        CallForPapersRequest request = new CallForPapersRequest(CALL_NAME, DESCRIPTION, OPEN_DATE, CLOSE_DATE);
        when(congressService.findCongressEntityById(ID_CONGRESS)).thenReturn(inactiveCongress());

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> callForPapersService.createCallForPapers(ID_CONGRESS, request));

        verify(callForPapersRepository, never()).save(any());
    }

    @Test
    void testCreateCallForPapersWhenCloseDateIsAfterCongressEndDate() throws NotFoundException {
        // Arrange — close date is beyond the congress end date
        LocalDateTime closeDateAfterEnd = CONGRESS_END_DATE.plusDays(5).atStartOfDay();
        CallForPapersRequest request = new CallForPapersRequest(CALL_NAME, DESCRIPTION, OPEN_DATE, closeDateAfterEnd);

        when(congressService.findCongressEntityById(ID_CONGRESS)).thenReturn(activeCongress());

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> callForPapersService.createCallForPapers(ID_CONGRESS, request));

        verify(callForPapersRepository, never()).save(any());
    }

    @Test
    void testCreateCallForPapersWhenOpenCallAlreadyExists() throws NotFoundException {
        // Arrange
        CallForPapersRequest request = new CallForPapersRequest(CALL_NAME, DESCRIPTION, OPEN_DATE, CLOSE_DATE);
        when(congressService.findCongressEntityById(ID_CONGRESS)).thenReturn(activeCongress());
        when(callForPapersRepository.existsByCongress_IdCongressAndIsOpenTrue(ID_CONGRESS)).thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> callForPapersService.createCallForPapers(ID_CONGRESS, request));

        verify(callForPapersRepository, never()).save(any());
    }

    //---------------- TESTS FOR DELETE CALL FOR PAPERS --------------
    @Test
    void testDeleteCallForPapers() throws Exception {
        // Arrange
        CallForPapersEntity callEntity = callForPapersEntity(true);
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(callEntity));
        when(submissionRepository.existsByCallForPapers_IdCall(ID_CALL)).thenReturn(false);

        // Act
        callForPapersService.deleteCallForPapers(ID_CALL);

        // Assert
        verify(callForPapersRepository).delete(callEntity);
    }

    @Test
    void testDeleteCallForPapersWhenNotFound() {
        // Arrange
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> callForPapersService.deleteCallForPapers(ID_CALL));

        verify(callForPapersRepository, never()).delete(any());
    }

    @Test
    void testDeleteCallForPapersWhenHasSubmissions() {
        // Arrange
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(callForPapersEntity(true)));
        when(submissionRepository.existsByCallForPapers_IdCall(ID_CALL)).thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> callForPapersService.deleteCallForPapers(ID_CALL));

        verify(callForPapersRepository, never()).delete(any());
    }

    //---------------- TESTS FOR CLOSE CALL FOR PAPERS --------------

    @Test
    void testCloseCallForPapers() throws Exception {
        // Arrange
        CallForPapersEntity callEntity = callForPapersEntity(true);
        ArgumentCaptor<CallForPapersEntity> captor = ArgumentCaptor.forClass(CallForPapersEntity.class);

        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(callEntity));
        when(callForPapersRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        callForPapersService.closeCallForPapers(ID_CALL);

        // Assert
        assertAll(
                () -> verify(callForPapersRepository).save(captor.capture()),
                () -> assertFalse(captor.getValue().getIsOpen())
        );
    }

    @Test
    void testCloseCallForPapersWhenNotFound() {
        // Arrange
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> callForPapersService.closeCallForPapers(ID_CALL));

        verify(callForPapersRepository, never()).save(any());
    }

    //---------------- TESTS FOR FIND CALL FOR PAPERS BY ID --------------

    @Test
    void testFindCallForPapersEntityById() throws Exception {
        // Arrange
        CallForPapersEntity callEntity = callForPapersEntity(true);
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.of(callEntity));

        // Act
        CallForPapersEntity result = callForPapersService.findCallForPapersEntityById(ID_CALL);

        // Assert
        assertAll(
                () -> assertEquals(ID_CALL,   result.getIdCall()),
                () -> assertEquals(CALL_NAME, result.getCallName()),
                () -> assertTrue(result.getIsOpen())
        );
    }

    @Test
    void testFindCallForPapersEntityByIdWhenNotFound() {
        // Arrange
        when(callForPapersRepository.findById(ID_CALL)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> callForPapersService.findCallForPapersEntityById(ID_CALL));
    }

    //---------------- TESTS FOR GET ALL CALL FOR PAPERS BY ADMIN CONGRESS --------------

    @Test
    void testGetAllCallForPapersByAdminCongress() throws Exception {
        // Arrange
        CongressResponse congressResponse = congressResponse();
        when(congressAdministratorService.getCongressesByAdministrator(ID_ADMIN_CONGRESS))
                .thenReturn(List.of(congressResponse));
        when(callForPapersRepository.findByCongress_IdCongress(ID_CONGRESS))
                .thenReturn(List.of(callForPapersEntity(true), callForPapersEntity(false)));

        // Act
        List<CallForPapersDetailsResponse> result =
                callForPapersService.getAllCallForPapersByAdminCongress(ID_ADMIN_CONGRESS);

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(CALL_NAME, result.get(0).getCallName())
        );
    }

    @Test
    void testGetAllCallForPapersByAdminCongressReturnsEmptyWhenNoCongresses() throws Exception {
        // Arrange
        when(congressAdministratorService.getCongressesByAdministrator(ID_ADMIN_CONGRESS))
                .thenReturn(List.of());

        // Act
        List<CallForPapersDetailsResponse> result =
                callForPapersService.getAllCallForPapersByAdminCongress(ID_ADMIN_CONGRESS);

        // Assert
        assertTrue(result.isEmpty());
        verify(callForPapersRepository, never()).findByCongress_IdCongress(any());
    }

    @Test
    void testGetAllCallForPapersByAdminCongressWhenAdminNotFound() throws NotFoundException {
        // Arrange
        when(congressAdministratorService.getCongressesByAdministrator(ID_ADMIN_CONGRESS))
                .thenThrow(new NotFoundException("Admin not found"));

        // Assert
        assertThrows(NotFoundException.class,
                () -> callForPapersService.getAllCallForPapersByAdminCongress(ID_ADMIN_CONGRESS));
    }

    //---------------- TESTS FOR ELEGIBLE CONGRESSES FOR CALL FOR PAPERS --------------

    @Test
    void testElegibleCongressesForCallForPapers() throws Exception {
        // Arrange — one congress with open call (not eligible), one without (eligible)
        CongressResponse withOpenCall    = congressResponse();          // ID_CONGRESS
        CongressResponse withoutOpenCall = congressResponseSecond();    // different id

        when(congressAdministratorService.getCongressesByAdministrator(ID_ADMIN_CONGRESS))
                .thenReturn(List.of(withOpenCall, withoutOpenCall));
        when(callForPapersRepository.existsByCongress_IdCongressAndIsOpenTrue(ID_CONGRESS)).thenReturn(true);
        when(callForPapersRepository.existsByCongress_IdCongressAndIsOpenTrue(20L)).thenReturn(false);

        // Act
        List<CongressResponse> result =
                callForPapersService.elegibleCongressesForCallForPapers(ID_ADMIN_CONGRESS);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(20L, result.get(0).getIdCongress())
        );
    }

    @Test
    void testElegibleCongressesExcludesInactiveCongresses() throws Exception {
        // Arrange
        CongressResponse inactiveCongressResponse = inactiveCongressResponse();

        when(congressAdministratorService.getCongressesByAdministrator(ID_ADMIN_CONGRESS))
                .thenReturn(List.of(inactiveCongressResponse));
        when(callForPapersRepository.existsByCongress_IdCongressAndIsOpenTrue(ID_CONGRESS)).thenReturn(false);

        // Act
        List<CongressResponse> result =
                callForPapersService.elegibleCongressesForCallForPapers(ID_ADMIN_CONGRESS);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testElegibleCongressesReturnsEmptyWhenAllHaveOpenCalls() throws Exception {
        // Arrange
        when(congressAdministratorService.getCongressesByAdministrator(ID_ADMIN_CONGRESS))
                .thenReturn(List.of(congressResponse()));
        when(callForPapersRepository.existsByCongress_IdCongressAndIsOpenTrue(ID_CONGRESS)).thenReturn(true);

        // Act
        List<CongressResponse> result =
                callForPapersService.elegibleCongressesForCallForPapers(ID_ADMIN_CONGRESS);

        // Assert
        assertTrue(result.isEmpty());
    }

    //---------------- TESTS FOR GET OPEN CALL FOR PAPERS BY CONGRESS ID --------------

    @Test
    void testGetCallForPapersOpenByCongressId() throws Exception {
        // Arrange
        when(congressService.findCongressEntityById(ID_CONGRESS)).thenReturn(activeCongress());
        when(callForPapersRepository.findByCongress_IdCongressAndIsOpenTrue(ID_CONGRESS))
                .thenReturn(Optional.of(callForPapersEntity(true)));

        // Act
        CallForPapersDetailsResponse result =
                callForPapersService.getCallForPapersOpenByCongressId(ID_CONGRESS);

        // Assert
        assertAll(
                () -> assertEquals(CALL_NAME,     result.getCallName()),
                () -> assertEquals(CONGRESS_NAME, result.getCongress().getCongressName()),
                () -> assertTrue(result.getIsOpen())
        );
    }

    @Test
    void testGetCallForPapersOpenByCongressIdWhenCongressNotFound() throws NotFoundException {
        // Arrange
        when(congressService.findCongressEntityById(ID_CONGRESS))
                .thenThrow(new NotFoundException("Congress not found"));

        // Assert
        assertThrows(NotFoundException.class,
                () -> callForPapersService.getCallForPapersOpenByCongressId(ID_CONGRESS));
    }

    @Test
    void testGetCallForPapersOpenByCongressIdWhenNoOpenCallExists() throws NotFoundException {
        // Arrange
        when(congressService.findCongressEntityById(ID_CONGRESS)).thenReturn(activeCongress());
        when(callForPapersRepository.findByCongress_IdCongressAndIsOpenTrue(ID_CONGRESS))
                .thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> callForPapersService.getCallForPapersOpenByCongressId(ID_CONGRESS));
    }

    //----------- HELPER METHODS -----------

    private CongressEntity activeCongress() {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(ID_CONGRESS);
        congress.setCongressName(CONGRESS_NAME);
        congress.setIsActive(true);
        congress.setEndDate(CONGRESS_END_DATE);
        congress.setInstitution(institution());
        congress.setStartDate(LocalDate.now());
        congress.setPrice(new BigDecimal("100.00"));
        return congress;
    }
    private InstitutionEntity institution() {
        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(1L);
        institution.setInstitutionName("USAC");
        return institution;
    }

    private CongressEntity inactiveCongress() {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(ID_CONGRESS);
        congress.setCongressName(CONGRESS_NAME);
        congress.setIsActive(false);
        congress.setEndDate(CONGRESS_END_DATE);
        congress.setInstitution(institution());
        congress.setStartDate(LocalDate.now());
        congress.setPrice(new BigDecimal("100.00"));
        return congress;
    }

    private CallForPapersEntity callForPapersEntity(boolean isOpen) {
        CallForPapersEntity entity = new CallForPapersEntity();
        entity.setIdCall(ID_CALL);
        entity.setCallName(CALL_NAME);
        entity.setDescription(DESCRIPTION);
        entity.setOpenDate(OPEN_DATE);
        entity.setCloseDate(CLOSE_DATE);
        entity.setIsOpen(isOpen);
        entity.setCongress(activeCongress());
        return entity;
    }

    private CallForPapersEntity savedCall(CallForPapersEntity base) {
        base.setIdCall(ID_CALL);
        base.setIsOpen(true);
        return base;
    }

    private CongressResponse congressResponse() {
        return new CongressResponse(ID_CONGRESS, "USAC", CONGRESS_NAME, "Description",
                LocalDate.now().toString(), CONGRESS_END_DATE.toString(), "Guatemala", "100.00", true);
    }
    private CongressResponse congressResponseSecond() {
        return new CongressResponse(20L, "CUNOC", "Second Congress", "Description",
                LocalDate.now().toString(), CONGRESS_END_DATE.toString(), "Quetzaltenango", "150.00", true);
    }

    private CongressResponse inactiveCongressResponse() {
        return new CongressResponse(ID_CONGRESS, "USAC", CONGRESS_NAME, "Description",
                LocalDate.now().toString(), CONGRESS_END_DATE.toString(), "Guatemala", "100.00", false);
    }
}