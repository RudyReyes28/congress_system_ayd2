package com.alessandro.congress_management.services.workshopreservation;

import com.alessandro.congress_management.dto.workshopreservation.CountWorkshopReservationsResponse;
import com.alessandro.congress_management.dto.workshopreservation.WorkShopReservationDetailsResponse;
import com.alessandro.congress_management.dto.workshopreservation.WorkShopReservationResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.workshop_reservation.WorkshopReservationEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.congressadministrator.CongressAdministratorRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.repositories.workshopreservation.WorkshopReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkShopReservationServiceImpl implements WorkShopReservationService{
    private final String WORKSHOP_TYPE_NAME = "TALLER";

    private final WorkshopReservationRepository workShopReservationRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final RegistrationRepository registrationRepository;
    private final CongressAdministratorRepository congressAdministratorRepository;
    private final ActivityTypeRepository activityTypeRepository;

    public WorkShopReservationServiceImpl(WorkshopReservationRepository workShopReservationRepository, UserRepository userRepository, ActivityRepository activityRepository, RegistrationRepository registrationRepository, CongressAdministratorRepository congressAdministratorRepository, ActivityTypeRepository activityTypeRepository) {
        this.workShopReservationRepository = workShopReservationRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.registrationRepository = registrationRepository;
        this.congressAdministratorRepository = congressAdministratorRepository;
        this.activityTypeRepository = activityTypeRepository;
    }


    @Override
    public WorkShopReservationResponse reserveWorkshop(Long idActivityWorkshop, Long idUser) throws NotFoundException, BusinessRuleException {
        // Validar que el usuario existe
        UserEntity user = getUserById(idUser);

        // Validar que la actividad existe
        ActivityEntity activity = getActivityById(idActivityWorkshop);

        // Validar que la actividad es un taller
        validateActivityIsWorkshop(idActivityWorkshop);

        // Validar que el usuario está registrado en el congreso asociado a la actividad
        validateUserRegistrationForCongress(idUser, activity.getCongress().getIdCongress());

        // Validar que la actividad no ha alcanzado su capacidad máxima
        validateWorkshopCapacity(activity);

        // Validar que el usuario no ha reservado ya esta actividad
        validateUserAlreadyReservedWorkshop(idUser, idActivityWorkshop);

        // Validar que la actividad es en el futuro
        validateActivityIsInTheFuture(activity);

        // Crear la reserva
        WorkshopReservationEntity reservationEntity = new WorkshopReservationEntity();
        reservationEntity.setActivity(activity);
        reservationEntity.setUser(user);
        reservationEntity.setReservedAt(LocalDateTime.now());

        WorkshopReservationEntity savedReservation = workShopReservationRepository.save(reservationEntity);
        return WorkShopReservationResponse.fromEntity(savedReservation);
    }

    @Override
    public void cancelWorkshopReservation(Long idRervation, Long idUser) throws BusinessRuleException, NotFoundException {
        // Validar que la reserva existe y que el usuario es el propietario de la reserva
        validateUserIsOwnerOfReservation(idUser, idRervation);
        //Obtenemos la reserva
        WorkshopReservationEntity reservation = getReservationById(idRervation);
        // Validar que la actividad asociada a la reserva es en el futuro
        validateActivityIsInTheFuture(reservation.getActivity());
        // Eliminar la reserva
        workShopReservationRepository.delete(reservation);

    }

    @Override
    public List<WorkShopReservationDetailsResponse> getMyWorkshopReservations(Long idUser) {
        List<WorkshopReservationEntity> reservations = workShopReservationRepository.findByUser_IdUser(idUser);
        return reservations.stream()
                .map(WorkShopReservationDetailsResponse::fromEntity)
                .toList();

    }

    @Override
    public CountWorkshopReservationsResponse countWorkshopReservations(Long idActivityWorkshop) throws NotFoundException {
        // Validar que la actividad existe
        ActivityEntity activity = getActivityById(idActivityWorkshop);
        int count = workShopReservationRepository.countByActivity_IdActivity(idActivityWorkshop);
        return CountWorkshopReservationsResponse.fromEntity(idActivityWorkshop, activity.getActivityName(), count);
    }

    @Override
    public List<WorkShopReservationResponse> getAllWorkshopReservationsByActivity(Long idActivityWorkshop, Long idUser) throws NotFoundException, BusinessRuleException {
        // Validar que la actividad existe
        ActivityEntity activity = getActivityById(idActivityWorkshop);
        //Verificar que el usuario sea administrador del congreso
        validateUserIsAdminOfCongress(idUser, activity.getCongress().getIdCongress());

        List<WorkshopReservationEntity> reservations = workShopReservationRepository.findByActivity_IdActivity(idActivityWorkshop);
        return reservations.stream()
                .map(WorkShopReservationResponse::fromEntity)
                .toList();
    }

    //Metodos auxiliares
    private UserEntity getUserById(Long idUser) throws NotFoundException {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ActivityEntity getActivityById(Long idActivity) throws NotFoundException {
        return activityRepository.findById(idActivity)
                .orElseThrow(() -> new NotFoundException("Activity not found"));
    }

    private WorkshopReservationEntity getReservationById(Long idReservation) throws NotFoundException {
        return workShopReservationRepository.findById(idReservation)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
    }

    private void validateUserRegistrationForCongress(Long idUser, Long idCongress) throws BusinessRuleException {
        if (!registrationRepository.existsByUser_IdUserAndCongress_IdCongress(idUser, idCongress)) {
            throw new BusinessRuleException("User is not registered for the congress");
        }
    }

    private void validateWorkshopCapacity(ActivityEntity activity) throws BusinessRuleException {
        int currentReservations = workShopReservationRepository.countByActivity_IdActivity(activity.getIdActivity());
        if (currentReservations >= activity.getMaxCapacity()) {
            throw new BusinessRuleException("Workshop is fully booked");
        }
    }

    private void validateUserAlreadyReservedWorkshop(Long idUser, Long idActivity) throws BusinessRuleException {
        if (workShopReservationRepository.existsByUser_IdUserAndActivity_IdActivity(idUser, idActivity)) {
            throw new BusinessRuleException("User has already reserved this workshop");
        }
    }

    private void validateUserIsOwnerOfReservation(Long idUser, Long idReservation) throws NotFoundException, BusinessRuleException {
        WorkshopReservationEntity reservation = workShopReservationRepository.findById(idReservation)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        if (!reservation.getUser().getIdUser().equals(idUser)) {
            throw new BusinessRuleException("User is not the owner of the reservation");
        }
    }

    private void  validateActivityIsInTheFuture(ActivityEntity activity) throws BusinessRuleException {
        if (activity.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot reserve a workshop that has already started");
        }
    }

    private void validateUserIsAdminOfCongress(Long idUser, Long idCongress) throws BusinessRuleException {

        if (!congressAdministratorRepository.existsByUser_IdUserAndCongress_IdCongress(idUser, idCongress)) {
            throw new BusinessRuleException("User is not an administrator of the congress");
        }

    }

    private void validateActivityIsWorkshop(Long idActivity) throws BusinessRuleException, NotFoundException {
        ActivityEntity activity = getActivityById(idActivity);
        if (!activity.getActivityType().getTypeName().equalsIgnoreCase(WORKSHOP_TYPE_NAME)) {
            throw new BusinessRuleException("The specified activity is not a workshop");
        }
    }
}
