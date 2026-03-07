package com.alessandro.congress_management.services.reports;

import com.alessandro.congress_management.dto.reports.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.SystemConfigurationEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.workshop_reservation.WorkshopReservationEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activitypresenter.ActivityPresenterRepository;
import com.alessandro.congress_management.repositories.attendance.AttendanceRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.system_configuration.SystemConfigurationRepository;
import com.alessandro.congress_management.repositories.workshopreservation.WorkshopReservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CongressAdminReportServiceImpl implements CongressAdminReportService {

    private static final String CONFIG_KEY_COMMISSION = "COMMISSION_PERCENTAGE";
    private static final String TYPE_TALLER = "TALLER";
    private static final String PART_ATTENDEE = "ATTENDEE";
    private static final String PART_PRESENTER = "PRESENTER";
    private static final String PART_INVITED = "INVITED_SPEAKER";

    private final CongressRepository congressRepository;
    private final CongressAdministratorRepository congressAdminRepository;
    private final RegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final ActivityRepository activityRepository;
    private final ActivityPresenterRepository activityPresenterRepository;
    private final WorkshopReservationRepository workshopReservationRepository;
    private final SystemConfigurationRepository systemConfigRepository;
    private final UserRepository userRepository;

    public CongressAdminReportServiceImpl(CongressRepository congressRepository,
                                          CongressAdministratorRepository congressAdminRepository,
                                          RegistrationRepository registrationRepository,
                                          AttendanceRepository attendanceRepository,
                                          ActivityRepository activityRepository,
                                          ActivityPresenterRepository activityPresenterRepository,
                                          WorkshopReservationRepository workshopReservationRepository,
                                          SystemConfigurationRepository systemConfigRepository,
                                          UserRepository userRepository) {
        this.congressRepository         = congressRepository;
        this.congressAdminRepository    = congressAdminRepository;
        this.registrationRepository     = registrationRepository;
        this.attendanceRepository       = attendanceRepository;
        this.activityRepository         = activityRepository;
        this.activityPresenterRepository = activityPresenterRepository;
        this.workshopReservationRepository = workshopReservationRepository;
        this.systemConfigRepository     = systemConfigRepository;
        this.userRepository             = userRepository;
    }

    @Override
    public ParticipantsReportResponse getParticipantsReport(Long idCongress, Long idAdmin,
                                                            String participationType)
            throws NotFoundException, BusinessRuleException {

        CongressEntity congress = findCongress(idCongress);
        validateCongressAdmin(idAdmin, idCongress);

       //base: inscription = ATTENDEE by default)
        List<UserEntity> registeredUsers =
                registrationRepository.findUsersByCongress_IdCongress(idCongress);

        // Todos los presentadores (incluyendo invitados) para marcar su participación
        List<Long> presenterUserIds = activityPresenterRepository
                .findByActivity_Congress_IdCongress(idCongress)
                .stream()
                .map(ap -> ap.getUser().getIdUser())
                .distinct()
                .toList();


        Map<Long, Set<String>> typesByUser = new HashMap<>();
        for (UserEntity user : registeredUsers) {
            typesByUser.computeIfAbsent(user.getIdUser(), k -> new HashSet<>()).add(PART_ATTENDEE);
        }


        activityPresenterRepository.findByActivity_Congress_IdCongress(idCongress)
                .forEach(ap -> {
                    String type = Boolean.TRUE.equals(ap.getIsInvitedSpeaker())
                            ? PART_INVITED : PART_PRESENTER;
                    typesByUser.computeIfAbsent(ap.getUser().getIdUser(), k -> new HashSet<>()).add(type);
                });

        // Coleccionar todos los IDs de usuario para obtener sus datos en una sola consulta
        Set<Long> allUserIds = typesByUser.keySet();
        Map<Long, UserEntity> usersById = userRepository.findAllById(allUserIds)
                .stream().collect(Collectors.toMap(UserEntity::getIdUser, u -> u));

        List<ParticipantsReportResponse.ParticipantDto> participants = allUserIds.stream()
                .filter(uid -> {
                    if (participationType == null || participationType.isBlank()) return true;
                    return typesByUser.getOrDefault(uid, Set.of()).contains(participationType);
                })
                .map(uid -> {
                    UserEntity u = usersById.get(uid);
                    List<String> types = new ArrayList<>(typesByUser.getOrDefault(uid, Set.of()));
                    return new ParticipantsReportResponse.ParticipantDto(
                            u.getIdUser(),
                            u.getIdentificationNumber(),
                            u.getFullName(),
                            u.getOrganization(),
                            u.getEmail(),
                            u.getPhoneNumber(),
                            types
                    );
                })
                .sorted(Comparator.comparing(ParticipantsReportResponse.ParticipantDto::getFullName))
                .toList();

        return new ParticipantsReportResponse(
                idCongress, congress.getCongressName(),
                participants.size(), participants);
    }

    @Override
    public ActivityAttendanceReportResponse getActivityAttendanceReport(
            Long idCongress, Long idAdmin,
            Long idActivity, Long idRoom,
            LocalDateTime startDate, LocalDateTime endDate)
            throws NotFoundException, BusinessRuleException {

        CongressEntity congress = findCongress(idCongress);
        validateCongressAdmin(idAdmin, idCongress);

        // Filtrar actividades del congreso por los parámetros opcionales
        List<ActivityEntity> activities = activityRepository
                .findByCongress_IdCongress(idCongress).stream()
                .filter(a -> idActivity == null || a.getIdActivity().equals(idActivity))
                .filter(a -> idRoom == null || a.getRoom().getIdRoom().equals(idRoom))
                .filter(a -> startDate == null || !a.getStartTime().isBefore(startDate))
                .filter(a -> endDate   == null || !a.getStartTime().isAfter(endDate))
                .toList();

        List<ActivityAttendanceReportResponse.ActivityAttendanceDto> dtos = activities.stream()
                .map(a -> {
                    int count = attendanceRepository.countByActivity_IdActivity(a.getIdActivity());
                    return new ActivityAttendanceReportResponse.ActivityAttendanceDto(
                            a.getIdActivity(),
                            a.getActivityName(),
                            a.getRoom().getRoomName(),
                            a.getStartTime().toString(),
                            a.getEndTime().toString(),
                            count
                    );
                })
                .toList();

        int total = dtos.stream().mapToInt(ActivityAttendanceReportResponse.ActivityAttendanceDto::getAttendanceCount).sum();

        return new ActivityAttendanceReportResponse(idCongress, congress.getCongressName(), dtos, total);
    }


    @Override
    public WorkshopReservationReportResponse getWorkshopReservationReport(
            Long idCongress, Long idAdmin, Long idActivity)
            throws NotFoundException, BusinessRuleException {

        CongressEntity congress = findCongress(idCongress);
        validateCongressAdmin(idAdmin, idCongress);

        // Solo consideramos actividades de tipo "TALLER" y aplicamos el filtro opcional de idActivity
        List<ActivityEntity> workshops = activityRepository
                .findByCongress_IdCongress(idCongress).stream()
                .filter(a -> TYPE_TALLER.equalsIgnoreCase(a.getActivityType().getTypeName()))
                .filter(a -> idActivity == null || a.getIdActivity().equals(idActivity))
                .toList();

        List<WorkshopReservationReportResponse.WorkshopSummaryDto> summaries = workshops.stream()
                .map(workshop -> {
                    List<WorkshopReservationEntity> reservations =
                            workshopReservationRepository.findByActivity_IdActivity(workshop.getIdActivity());

                    int capacity= workshop.getMaxCapacity() != null ? workshop.getMaxCapacity() : 0;
                    int reserved = reservations.size();
                    int available= Math.max(0, capacity - reserved);

                    List<WorkshopReservationReportResponse.ReservedParticipantDto> participants =
                            reservations.stream()
                                    .map(r -> new WorkshopReservationReportResponse.ReservedParticipantDto(
                                            r.getUser().getIdUser(),
                                            r.getUser().getIdentificationNumber(),
                                            r.getUser().getFullName(),
                                            r.getUser().getEmail()
                                    ))
                                    .toList();

                    return new WorkshopReservationReportResponse.WorkshopSummaryDto(
                            workshop.getIdActivity(),
                            workshop.getActivityName(),
                            capacity, reserved, available,
                            participants
                    );
                })
                .toList();

        return new WorkshopReservationReportResponse(
                idCongress, congress.getCongressName(), summaries);
    }


    @Override
    public CongressEarningsReportResponse getCongressEarningsReport(Long idCongress, Long idAdmin)
            throws NotFoundException, BusinessRuleException {

        CongressEntity congress = findCongress(idCongress);
        validateCongressAdmin(idAdmin, idCongress);

        BigDecimal commissionPct = getCommissionPercentage();

        BigDecimal totalRevenue = registrationRepository.sumAmountPaidByCongressId(idCongress);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        int totalRegistrations = registrationRepository.countByCongress_IdCongress(idCongress);

        BigDecimal commissionAmount = totalRevenue
                .multiply(commissionPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal netEarnings = totalRevenue.subtract(commissionAmount);

        return new CongressEarningsReportResponse(
                congress.getIdCongress(),
                congress.getCongressName(),
                congress.getLocation(),
                congress.getStartDate(),
                congress.getEndDate(),
                congress.getPrice(),
                totalRegistrations,
                totalRevenue,
                commissionAmount,
                netEarnings
        );
    }


    private CongressEntity findCongress(Long id) throws NotFoundException {
        return congressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Congress not found"));
    }

    private void validateCongressAdmin(Long idAdmin, Long idCongress) throws BusinessRuleException {
        if (!congressAdminRepository.existsByUser_IdUserAndCongress_IdCongress(idAdmin, idCongress)) {
            throw new BusinessRuleException("User is not an administrator of this congress.");
        }
    }

    private BigDecimal getCommissionPercentage() throws NotFoundException {
        SystemConfigurationEntity config = systemConfigRepository
                .findByConfigKey(CONFIG_KEY_COMMISSION)
                .orElseThrow(() -> new NotFoundException("Commission configuration not found"));
        return new BigDecimal(config.getConfigValue());
    }
}