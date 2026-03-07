package com.alessandro.congress_management.services.reports;

import com.alessandro.congress_management.dto.reports.CongressByInstitutionReportResponse;
import com.alessandro.congress_management.dto.reports.EarningsReportResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.institution.InstitutionRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.system_configuration.SystemConfigurationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SystemAdminReportServiceImplTest {


    private static final Long   ID_ADMIN        = 1L;
    private static final Long   ID_INSTITUTION_A = 10L;
    private static final Long   ID_INSTITUTION_B = 11L;
    private static final Long   ID_CONGRESS_1   = 20L;
    private static final Long   ID_CONGRESS_2   = 21L;

    private static final String INSTITUTION_A   = "Alpha University";
    private static final String INSTITUTION_B   = "Beta Institute";
    private static final String CONGRESS_1_NAME = "Tech Summit";
    private static final String CONGRESS_2_NAME = "Data Congress";

    private static final LocalDate START_DATE   = LocalDate.of(2026, 1, 1);
    private static final LocalDate END_DATE     = LocalDate.of(2026, 12, 31);

    private static final String COMMISSION      = "15"; // 15%


    @Mock private CongressRepository          congressRepository;
    @Mock private InstitutionRepository       institutionRepository;
    @Mock private RegistrationRepository      registrationRepository;
    @Mock private SystemConfigurationRepository systemConfigRepository;
    //@Mock private SystemAdminRepository       systemAdminRepository;

    @InjectMocks
    private SystemAdminReportServiceImpl service;

    // ---------------- TESTS FOR GET EARNINGS REPORT ---------------------------------------

    @Test
    void testGetEarningsReport_WithAllInstitutions() throws Exception {
        // Arrange — two congresses from two different institutions
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.of(configEntity(COMMISSION)));
        when(congressRepository.findByStartDateBetween(START_DATE, END_DATE))
                .thenReturn(List.of(congress1(institutionA()), congress2(institutionB())));
        when(registrationRepository.sumAmountPaidByCongressId(ID_CONGRESS_1))
                .thenReturn(new BigDecimal("1000.00"));
        when(registrationRepository.countByCongress_IdCongress(ID_CONGRESS_1)).thenReturn(10);
        when(registrationRepository.sumAmountPaidByCongressId(ID_CONGRESS_2))
                .thenReturn(new BigDecimal("500.00"));
        when(registrationRepository.countByCongress_IdCongress(ID_CONGRESS_2)).thenReturn(5);

        // Act
        EarningsReportResponse result = service.getEarningsReport(START_DATE, END_DATE, null, ID_ADMIN);

        // Assert
        assertAll(
                () -> assertEquals(2,                       result.getInstitutions().size()),
                () -> assertEquals(new BigDecimal("1500.00"), result.getTotalRevenue()),
                () -> assertEquals(new BigDecimal("225.00"),  result.getTotalSystemEarnings()), // 15% of 1500
                // Institutions sorted alphabetically: Alpha before Beta
                () -> assertEquals(INSTITUTION_A, result.getInstitutions().get(0).getInstitutionName()),
                () -> assertEquals(INSTITUTION_B, result.getInstitutions().get(1).getInstitutionName()),
                // Alpha has 1000 revenue, 150 earnings
                () -> assertEquals(new BigDecimal("1000.00"), result.getInstitutions().get(0).getInstitutionTotalRevenue()),
                () -> assertEquals(new BigDecimal("150.00"),  result.getInstitutions().get(0).getInstitutionTotalEarnings())
        );
    }

    @Test
    void testGetEarningsReport_FilteredByInstitution() throws Exception {
        // Arrange — only institution A filtered
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.of(configEntity(COMMISSION)));
        when(congressRepository.findByStartDateBetweenAndInstitution_IdInstitution(
                START_DATE, END_DATE, ID_INSTITUTION_A))
                .thenReturn(List.of(congress1(institutionA())));
        when(registrationRepository.sumAmountPaidByCongressId(ID_CONGRESS_1))
                .thenReturn(new BigDecimal("1000.00"));
        when(registrationRepository.countByCongress_IdCongress(ID_CONGRESS_1)).thenReturn(10);

        // Act
        EarningsReportResponse result =
                service.getEarningsReport(START_DATE, END_DATE, ID_INSTITUTION_A, ID_ADMIN);

        // Assert
        assertAll(
                () -> assertEquals(1,   result.getInstitutions().size()),
                () -> assertEquals(INSTITUTION_A, result.getInstitutions().get(0).getInstitutionName()),
                () -> assertEquals(new BigDecimal("1000.00"), result.getTotalRevenue()),
                () -> assertEquals(new BigDecimal("150.00"),  result.getTotalSystemEarnings())
        );
    }

    @Test
    void testGetEarningsReport_CongressesWithinInstitutionSortedByEarningsDesc() throws Exception {
        // Arrange — one institution with two congresses; congress2 earns more
        InstitutionEntity inst = institutionA();
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.of(configEntity(COMMISSION)));
        when(congressRepository.findByStartDateBetween(START_DATE, END_DATE))
                .thenReturn(List.of(congress1(inst), congress2(inst))); // both same institution
        // congress1 = 200, congress2 = 800
        when(registrationRepository.sumAmountPaidByCongressId(ID_CONGRESS_1))
                .thenReturn(new BigDecimal("200.00"));
        when(registrationRepository.countByCongress_IdCongress(ID_CONGRESS_1)).thenReturn(2);
        when(registrationRepository.sumAmountPaidByCongressId(ID_CONGRESS_2))
                .thenReturn(new BigDecimal("800.00"));
        when(registrationRepository.countByCongress_IdCongress(ID_CONGRESS_2)).thenReturn(8);

        // Act
        EarningsReportResponse result =
                service.getEarningsReport(START_DATE, END_DATE, null, ID_ADMIN);

        // Assert — congress2 (800) appears before congress1 (200) within the institution
        List<EarningsReportResponse.CongressEarningsDto> congresses =
                result.getInstitutions().get(0).getCongresses();
        assertAll(
                () -> assertEquals(2, congresses.size()),
                () -> assertEquals(CONGRESS_2_NAME, congresses.get(0).getCongressName()), // higher earnings first
                () -> assertEquals(CONGRESS_1_NAME, congresses.get(1).getCongressName())
        );
    }

    @Test
    void testGetEarningsReport_WhenCongressHasNoRegistrations_RevenueIsZero() throws Exception {
        // Arrange — sumAmountPaid returns null (empty congress)
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.of(configEntity(COMMISSION)));
        when(congressRepository.findByStartDateBetween(START_DATE, END_DATE))
                .thenReturn(List.of(congress1(institutionA())));
        when(registrationRepository.sumAmountPaidByCongressId(ID_CONGRESS_1)).thenReturn(null);
        when(registrationRepository.countByCongress_IdCongress(ID_CONGRESS_1)).thenReturn(0);

        // Act
        EarningsReportResponse result =
                service.getEarningsReport(START_DATE, END_DATE, null, ID_ADMIN);

        // Assert
        assertAll(
                () -> assertEquals(BigDecimal.ZERO, result.getTotalRevenue()),
                () -> assertEquals(new BigDecimal("0.00"), result.getTotalSystemEarnings())
        );
    }

    @Test
    void testGetEarningsReport_ReturnsEmptyWhenNoCongressesInRange() throws Exception {
        // Arrange
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.of(configEntity(COMMISSION)));
        when(congressRepository.findByStartDateBetween(START_DATE, END_DATE)).thenReturn(List.of());

        // Act
        EarningsReportResponse result =
                service.getEarningsReport(START_DATE, END_DATE, null, ID_ADMIN);

        // Assert
        assertAll(
                () -> assertTrue(result.getInstitutions().isEmpty()),
                () -> assertEquals(BigDecimal.ZERO, result.getTotalRevenue()),
                () -> assertEquals(new BigDecimal("0.00"), result.getTotalSystemEarnings())
        );
    }

    /*@Test
    void testGetEarningsReport_WhenUserIsNotSystemAdmin() {
        // Arrange
        when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(false);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.getEarningsReport(START_DATE, END_DATE, null, ID_ADMIN));

        verifyNoInteractions(congressRepository, registrationRepository);
    }*/

    /*@Test
    void testGetEarningsReport_WhenStartDateAfterEndDate() {
        // Arrange
        when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.getEarningsReport(END_DATE, START_DATE, null, ID_ADMIN));
    }*/

    @Test
    void testGetEarningsReport_WhenCommissionConfigNotFound() {
        // Arrange
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(systemConfigRepository.findByConfigKey("COMMISSION_PERCENTAGE"))
                .thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class,
                () -> service.getEarningsReport(START_DATE, END_DATE, null, ID_ADMIN));
    }

    // ------------ TESTS FOR GET CONGRESS BY INSTITUTION REPORT ---------------------------------------

    @Test
    void testGetCongressByInstitutionReport_AllInstitutions() throws Exception {
        // Arrange
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(congressRepository.findByStartDateBetween(START_DATE, END_DATE))
                .thenReturn(List.of(congress1(institutionA()), congress2(institutionB())));

        // Act
        CongressByInstitutionReportResponse result =
                service.getCongressByInstitutionReport(START_DATE, END_DATE, null, ID_ADMIN);

        // Assert
        assertAll(
                () -> assertEquals(2, result.getTotalCongresses()),
                () -> assertEquals(2, result.getInstitutions().size()),
                // Alphabetical order: Alpha before Beta
                () -> assertEquals(INSTITUTION_A, result.getInstitutions().get(0).getInstitutionName()),
                () -> assertEquals(INSTITUTION_B, result.getInstitutions().get(1).getInstitutionName()),
                () -> assertEquals(1, result.getInstitutions().get(0).getCongresses().size()),
                () -> assertEquals(CONGRESS_1_NAME, result.getInstitutions().get(0).getCongresses().get(0).getCongressName())
        );
    }

    @Test
    void testGetCongressByInstitutionReport_FilteredByInstitution() throws Exception {
        // Arrange
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(congressRepository.findByStartDateBetweenAndInstitution_IdInstitution(
                START_DATE, END_DATE, ID_INSTITUTION_A))
                .thenReturn(List.of(congress1(institutionA())));

        // Act
        CongressByInstitutionReportResponse result =
                service.getCongressByInstitutionReport(START_DATE, END_DATE, ID_INSTITUTION_A, ID_ADMIN);

        // Assert
        assertAll(
                () -> assertEquals(1, result.getTotalCongresses()),
                () -> assertEquals(1, result.getInstitutions().size()),
                () -> assertEquals(INSTITUTION_A, result.getInstitutions().get(0).getInstitutionName())
        );
    }

    @Test
    void testGetCongressByInstitutionReport_ReturnsEmptyWhenNoCongressesInRange() throws Exception {
        // Arrange
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);
        when(congressRepository.findByStartDateBetween(START_DATE, END_DATE)).thenReturn(List.of());

        // Act
        CongressByInstitutionReportResponse result =
                service.getCongressByInstitutionReport(START_DATE, END_DATE, null, ID_ADMIN);

        // Assert
        assertAll(
                () -> assertEquals(0, result.getTotalCongresses()),
                () -> assertTrue(result.getInstitutions().isEmpty())
        );
    }

    /*@Test
    void testGetCongressByInstitutionReport_WhenUserIsNotSystemAdmin() {
        // Arrange
        //when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(false);

        // Assert
        assertThrows(BusinessRuleException.class,
                () -> service.getCongressByInstitutionReport(START_DATE, END_DATE, null, ID_ADMIN));

        verifyNoInteractions(congressRepository);
    }*/

    /*@Test
    void testGetCongressByInstitutionReport_WhenStartDateAfterEndDate() {
        when(systemAdminRepository.existsByUser_IdUser(ID_ADMIN)).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> service.getCongressByInstitutionReport(END_DATE, START_DATE, null, ID_ADMIN));
    }*/

    //---------- METHODS -------------------

    private InstitutionEntity institutionA() {
        InstitutionEntity i = new InstitutionEntity();
        i.setIdInstitution(ID_INSTITUTION_A);
        i.setInstitutionName(INSTITUTION_A);
        i.setIsActive(true);
        return i;
    }

    private InstitutionEntity institutionB() {
        InstitutionEntity i = new InstitutionEntity();
        i.setIdInstitution(ID_INSTITUTION_B);
        i.setInstitutionName(INSTITUTION_B);
        i.setIsActive(true);
        return i;
    }

    private CongressEntity congress1(InstitutionEntity institution) {
        CongressEntity c = new CongressEntity();
        c.setIdCongress(ID_CONGRESS_1);
        c.setCongressName(CONGRESS_1_NAME);
        c.setDescription("Description 1");
        c.setLocation("Guatemala");
        c.setStartDate(LocalDate.of(2026, 3, 10));
        c.setEndDate(LocalDate.of(2026, 3, 12));
        c.setPrice(new BigDecimal("100.00"));
        c.setIsActive(true);
        c.setInstitution(institution);
        return c;
    }

    private CongressEntity congress2(InstitutionEntity institution) {
        CongressEntity c = new CongressEntity();
        c.setIdCongress(ID_CONGRESS_2);
        c.setCongressName(CONGRESS_2_NAME);
        c.setDescription("Description 2");
        c.setLocation("Quetzaltenango");
        c.setStartDate(LocalDate.of(2026, 6, 1));
        c.setEndDate(LocalDate.of(2026, 6, 3));
        c.setPrice(new BigDecimal("100.00"));
        c.setIsActive(true);
        c.setInstitution(institution);
        return c;
    }

    private SystemConfigurationEntity configEntity(String value) {
        SystemConfigurationEntity cfg = new SystemConfigurationEntity();
        cfg.setConfigKey("COMMISSION_PERCENTAGE");
        cfg.setConfigValue(value);
        return cfg;
    }
}