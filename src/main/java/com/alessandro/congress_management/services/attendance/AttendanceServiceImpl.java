package com.alessandro.congress_management.services.attendance;

import com.alessandro.congress_management.dto.attendance.AttendanceDetailsResponse;
import com.alessandro.congress_management.dto.attendance.AttendanceResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import com.alessandro.congress_management.models.attendance.ParticipationTypeEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.activitypresenter.ActivityPresenterRepository;
import com.alessandro.congress_management.repositories.attendance.AttendanceRepository;
import com.alessandro.congress_management.repositories.attendance.ParticipationTypeRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congress.CongressRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.workshopreservation.WorkshopReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class AttendanceServiceImpl implements AttendanceService{
    private final String ACTIVITY_TYPE_TALLER= "TALLER";
    private final String ACTIVITY_TYPE_PONENCIA= "PONENCIA";

    private final String PARTICIPATION_TYPE_ASISTENTE= "ATTENDEE";
    private final String PARTICIPATION_TYPE_PONENTE= "PRESENTER";
    private final String PARTICIPATION_TYPE_INVITED= "INVITED_SPEAKER";


    private final WorkshopReservationRepository workShopReservationRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivityPresenterRepository activityPresenterRepository;
    private final CongressRepository congressRepository;
    private final RegistrationRepository registrationRepository;
    private final CongressAdministratorRepository congressAdministratorRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ParticipationTypeRepository participationTypeRepository;

    public AttendanceServiceImpl(WorkshopReservationRepository workShopReservationRepository, UserRepository userRepository, ActivityRepository activityRepository, ActivityPresenterRepository activityPresenterRepository, CongressRepository congressRepository, RegistrationRepository registrationRepository, CongressAdministratorRepository congressAdministratorRepository, ActivityTypeRepository activityTypeRepository, AttendanceRepository attendanceRepository, ParticipationTypeRepository participationTypeRepository) {
        this.workShopReservationRepository = workShopReservationRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.activityPresenterRepository = activityPresenterRepository;
        this.congressRepository = congressRepository;
        this.registrationRepository = registrationRepository;
        this.congressAdministratorRepository = congressAdministratorRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.attendanceRepository = attendanceRepository;
        this.participationTypeRepository = participationTypeRepository;
    }

    @Override
    public AttendanceResponse recordAttendance(Long idActivity, Long idAdminUser, Long idUser) throws NotFoundException, BusinessRuleException {
        //Obtener la actividad
        ActivityEntity activity = getActivityById(idActivity);
        //Obtener el usuario
        UserEntity user = getUserById(idUser);
        //Obtener el usuario admin
        UserEntity adminUser = getUserById(idAdminUser);
        //Validar que el usuario admin es administrador del congreso asociado a la actividad
        validateAdminUserIsCongressAdministrator(idAdminUser, activity.getCongress().getIdCongress());
        //Validar que la asistencia se registra dentro del rango permitido (30 minutos antes de iniciar hasta 30 minutos después de finalizar)
        validateAttendanceTimeRange(activity);
        //Validar que la asistencia no ha sido registrada previamente para este usuario y actividad
        validateAttendanceNotAlreadyRecorded(idUser, idActivity);

        //Si el usuario es el ponente de la actividad, registrar su asistencia como ponente
        ActivityPresenterEntity presenter =
                activityPresenterRepository.findByActivity_IdActivityAndUser_IdUser(idActivity, idUser);
        if(presenter != null) {

            ParticipationTypeEntity presenterParticipationType = presenter.getIsInvitedSpeaker() ?
                    getParticipationTypeByName(PARTICIPATION_TYPE_INVITED) :
                    getParticipationTypeByName(PARTICIPATION_TYPE_PONENTE);
            //Registrar asistencia como ponente
            AttendanceEntity attendance = buildAttendance(activity, user, presenterParticipationType, adminUser);
            attendanceRepository.save(attendance);
            return AttendanceResponse.fromEntity(attendance);
        }

        //Validar que el usuario está registrado en el congreso asociado a la actividad
        validateUserRegistrationForCongress(idUser, activity.getCongress().getIdCongress());
        //Si la actividad es un taller, verificar que el usuario es asistente del taller
        if(activity.getActivityType().getTypeName().equals(ACTIVITY_TYPE_TALLER)) {
            if(!isUserAttendeeOfActivityTaller(idUser, idActivity)) {
                throw new NotFoundException("User is not an attendee of the workshop");
            }
        }

        //Registrar asistencia como asistente
        AttendanceEntity attendance = buildAttendance(activity, user, getParticipationTypeByName(PARTICIPATION_TYPE_ASISTENTE), adminUser);
        attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(attendance);


        //REALIZANDO PRUEBA DE DEPLOY
    }




    @Override
    public List<AttendanceDetailsResponse> getMyAttendanceDetails(Long idUser) {
        List<AttendanceEntity> attendances = attendanceRepository.findByUser_IdUser(idUser);
        return attendances.stream()
                .map(AttendanceDetailsResponse::fromEntity)
                .toList();
    }

    @Override
    public List<AttendanceDetailsResponse> getAttendanceDetailsByActivity(Long idActivity, Long idAdminUser) throws NotFoundException {
        //Obtener la actividad
        ActivityEntity activity = getActivityById(idActivity);
        //Validar que el usuario admin es administrador del congreso asociado a la actividad
        validateAdminUserIsCongressAdministrator(idAdminUser, activity.getCongress().getIdCongress());
        List<AttendanceEntity> attendances = attendanceRepository.findByActivity_IdActivity(idActivity);
        return attendances.stream()
                .map(AttendanceDetailsResponse::fromEntity)
                .toList();
    }

    @Override
    public List<AttendanceDetailsResponse> getAttendanceDetailsByCongress(Long idCongress, Long idAdminUser) throws NotFoundException {
        //Obtener el congreso
        CongressEntity congress = getCongressById(idCongress);
        //Validar que el usuario admin es administrador del congreso
        validateAdminUserIsCongressAdministrator(idAdminUser, idCongress);
        List<AttendanceEntity> attendances = attendanceRepository.findByActivity_Congress_IdCongress(idCongress);
        return attendances.stream()
                .map(AttendanceDetailsResponse::fromEntity)
                .toList();
    }


    //-------- Helper methods -----------
    private UserEntity getUserById(Long idUser) throws NotFoundException {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ActivityEntity getActivityById(Long idActivity) throws NotFoundException {
        return activityRepository.findById(idActivity)
                .orElseThrow(() -> new NotFoundException("Activity not found"));
    }

    private CongressEntity getCongressById(Long idCongress) throws NotFoundException {
        return congressRepository.findById(idCongress)
                .orElseThrow(() -> new NotFoundException("Congress not found"));
    }

    private void validateAdminUserIsCongressAdministrator(Long idAdminUser, Long idCongress) throws NotFoundException {
        if (!congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(idAdminUser, idCongress)) {
            throw new NotFoundException("Admin user is not an administrator of the congress");
        }
    }

    private void validateUserRegistrationForCongress(Long idUser, Long idCongress) throws NotFoundException {
        if(!registrationRepository.existsByUser_IdUserAndCongress_IdCongress(idUser, idCongress)) {
            throw new NotFoundException("User is not registered for the congress");
        }
    }

    private boolean isUserPresenterOfActivity(Long idUser, Long idActivity) {
        return activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(idActivity, idUser);
    }

    private boolean isUserAttendeeOfActivityTaller(Long idUser, Long idActivity) {
        return workShopReservationRepository.existsByUser_IdUserAndActivity_IdActivity(idUser, idActivity);
    }

    private ParticipationTypeEntity getParticipationTypeByName(String name) throws NotFoundException {
        return participationTypeRepository.findByTypeName(name)
                .orElseThrow(() -> new NotFoundException("Participation type not found"));
    }

    private boolean isInvitedSpeakerOfActivity(Long idUser, Long idActivity) {
        return activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUserAndIsInvitedSpeakerTrue(idActivity, idUser);
    }

    private void validateAttendanceTimeRange(ActivityEntity activity) throws BusinessRuleException {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime allowedStart = activity.getStartTime().minusMinutes(30);
        LocalDateTime allowedEnd = activity.getEndTime().plusMinutes(30);

        if (now.isBefore(allowedStart) || now.isAfter(allowedEnd)) {
            throw new BusinessRuleException(
                    "Attendance can only be recorded from 30 minutes before the start " +
                            "until 30 minutes after the activity ends"
            );
        }
    }

    private void validateAttendanceNotAlreadyRecorded(Long idUser, Long idActivity) throws BusinessRuleException {
        if(attendanceRepository.existsByUser_IdUserAndActivity_IdActivity(idUser, idActivity)) {
            throw new BusinessRuleException("Attendance has already been recorded for this user and activity");
        }
    }

    private AttendanceEntity buildAttendance(ActivityEntity activity, UserEntity user,
                                             ParticipationTypeEntity type, UserEntity adminUser) {
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setActivity(activity);
        attendance.setUser(user);
        attendance.setParticipationType(type);
        attendance.setRecordedBy(adminUser);
        attendance.setRecordedAt(LocalDateTime.now());
        return attendance;
    }

}
