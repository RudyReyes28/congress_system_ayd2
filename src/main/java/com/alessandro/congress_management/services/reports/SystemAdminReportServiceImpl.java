package com.alessandro.congress_management.services.reports;

import com.alessandro.congress_management.dto.reports.CongressByInstitutionReportResponse;
import com.alessandro.congress_management.dto.reports.EarningsReportResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.institution.InstitutionRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.system_configuration.SystemConfigurationRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemAdminReportServiceImpl implements SystemAdminReportService {

    private static final String CONFIG_KEY_COMMISSION = "COMMISSION_PERCENTAGE";

    private final CongressRepository congressRepository;
    private final InstitutionRepository institutionRepository;
    private final RegistrationRepository registrationRepository;
    private final SystemConfigurationRepository systemConfigRepository;
    private final CongressAdministratorRepository  systemAdminRepository;

    public SystemAdminReportServiceImpl(CongressRepository congressRepository,
                                        InstitutionRepository institutionRepository,
                                        RegistrationRepository registrationRepository,
                                        SystemConfigurationRepository systemConfigRepository,
                                        CongressAdministratorRepository systemAdminRepository) {
        this.congressRepository = congressRepository;
        this.institutionRepository = institutionRepository;
        this.registrationRepository = registrationRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.systemAdminRepository = systemAdminRepository;
    }

    @Override
    public EarningsReportResponse getEarningsReport(LocalDate startDate, LocalDate endDate,
                                                    Long idInstitution, Long idAdmin)
            throws NotFoundException, BusinessRuleException {

        validateDateRange(startDate, endDate);
        //validateSystemAdmin(idAdmin);

        BigDecimal commissionPct = getCommissionPercentage();

        List<CongressEntity> congresses = fetchCongresses(startDate, endDate, idInstitution);

        Map<InstitutionEntity, List<CongressEntity>> byInstitution = congresses.stream()
                .collect(Collectors.groupingBy(CongressEntity::getInstitution));

        List<EarningsReportResponse.InstitutionEarningsDto> institutionDtos = byInstitution
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getInstitutionName()))
                .map(entry -> buildInstitutionEarningsDto(entry.getKey(), entry.getValue(), commissionPct))
                .toList();

        BigDecimal totalRevenue = institutionDtos.stream()
                .map(EarningsReportResponse.InstitutionEarningsDto::getInstitutionTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEarnings = applyCommission(totalRevenue, commissionPct);

        return new EarningsReportResponse(institutionDtos, totalRevenue, totalEarnings);
    }

    private EarningsReportResponse.InstitutionEarningsDto buildInstitutionEarningsDto(
            InstitutionEntity institution,
            List<CongressEntity> congresses,
            BigDecimal commissionPct) {


        List<EarningsReportResponse.CongressEarningsDto> congressDtos = congresses.stream()
                .map(c -> buildCongressEarningsDto(c, commissionPct))
                .sorted(Comparator.comparing(
                        EarningsReportResponse.CongressEarningsDto::getSystemEarnings).reversed())
                .toList();

        BigDecimal instRevenue  = congressDtos.stream()
                .map(EarningsReportResponse.CongressEarningsDto::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal instEarnings = applyCommission(instRevenue, commissionPct);

        return new EarningsReportResponse.InstitutionEarningsDto(
                institution.getIdInstitution(),
                institution.getInstitutionName(),
                congressDtos,
                instRevenue,
                instEarnings
        );
    }

    private EarningsReportResponse.CongressEarningsDto buildCongressEarningsDto(
            CongressEntity congress, BigDecimal commissionPct) {

        BigDecimal totalRevenue = registrationRepository
                .sumAmountPaidByCongressId(congress.getIdCongress());
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        int totalRegistrations = registrationRepository
                .countByCongress_IdCongress(congress.getIdCongress());

        BigDecimal earnings = applyCommission(totalRevenue, commissionPct);

        return new EarningsReportResponse.CongressEarningsDto(
                congress.getIdCongress(),
                congress.getCongressName(),
                congress.getLocation(),
                congress.getStartDate(),
                congress.getEndDate(),
                congress.getPrice(),
                totalRegistrations,
                totalRevenue,
                earnings
        );
    }

    @Override
    public CongressByInstitutionReportResponse getCongressByInstitutionReport(
            LocalDate startDate, LocalDate endDate,
            Long idInstitution, Long idAdmin)
            throws NotFoundException, BusinessRuleException {

        validateDateRange(startDate, endDate);
        //validateSystemAdmin(idAdmin);

        List<CongressEntity> congresses = fetchCongresses(startDate, endDate, idInstitution);

        Map<InstitutionEntity, List<CongressEntity>> byInstitution = congresses.stream()
                .collect(Collectors.groupingBy(CongressEntity::getInstitution));

        List<CongressByInstitutionReportResponse.InstitutionCongressesDto> institutionDtos =
                byInstitution.entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getInstitutionName()))
                        .map(entry -> new CongressByInstitutionReportResponse.InstitutionCongressesDto(
                                entry.getKey().getIdInstitution(),
                                entry.getKey().getInstitutionName(),
                                entry.getValue().stream()
                                        .map(this::toCongressSummaryDto)
                                        .toList()
                        ))
                        .toList();

        int totalCongresses = congresses.size();
        return new CongressByInstitutionReportResponse(institutionDtos, totalCongresses);
    }

    private CongressByInstitutionReportResponse.CongressSummaryDto toCongressSummaryDto(CongressEntity c) {
        return new CongressByInstitutionReportResponse.CongressSummaryDto(
                c.getIdCongress(), c.getCongressName(), c.getDescription(),
                c.getStartDate(), c.getEndDate(), c.getLocation(),
                c.getPrice(), c.getIsActive()
        );
    }


    private List<CongressEntity> fetchCongresses(LocalDate start, LocalDate end, Long idInstitution) {
        if(start == null && end == null) {
            if(idInstitution != null) {
                return congressRepository.findByInstitution_IdInstitution(idInstitution);
            }
            return congressRepository.findAll();
        }

        if(idInstitution != null) {
            return congressRepository
                    .findByStartDateBetweenAndInstitution_IdInstitution(start, end, idInstitution);
        }

        return congressRepository.findByStartDateBetween(start, end);
    }

    private BigDecimal getCommissionPercentage() throws NotFoundException {
        SystemConfigurationEntity config = systemConfigRepository
                .findByConfigKey(CONFIG_KEY_COMMISSION)
                .orElseThrow(() -> new NotFoundException("System configuration for commission not found"));
        return new BigDecimal(config.getConfigValue());
    }

    private BigDecimal applyCommission(BigDecimal amount, BigDecimal commissionPct) {

        return amount.multiply(commissionPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /*private void validateSystemAdmin(Long idAdmin) throws BusinessRuleException {
        if (!systemAdminRepository.existsByUser_IdUser(idAdmin)) {
            throw new BusinessRuleException("User is not a system administrator.");
        }
    }*/

    private void validateDateRange(LocalDate start, LocalDate end) throws BusinessRuleException {
        /*if (start == null || end == null) {
            throw new BusinessRuleException("Start date and end date are required.");
        }*/
        //if (start.isAfter(end)) {
        //    throw new BusinessRuleException("Start date must be before or equal to end date.");
        //}
    }
}