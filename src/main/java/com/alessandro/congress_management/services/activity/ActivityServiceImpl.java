package com.alessandro.congress_management.services.activity;

import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.activity.CreateActivityRequest;
import com.alessandro.congress_management.dto.activity.UpdateActivityRequest;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.repositories.activity.ActivityRepository;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import com.alessandro.congress_management.repositories.attendance.AttendanceRepository;
import com.alessandro.congress_management.repositories.workshopreservation.WorkshopReservationRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.room.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {
    private static final String TALLER = "TALLER";

    private final ActivityRepository activityRepository;
    private final CongressService congressService;
    private final RoomService roomService;
    private final WorkshopReservationRepository workShopReservationRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final AttendanceRepository attendanceRepository;

    public ActivityServiceImpl(ActivityRepository activityRepository, CongressService congressService, RoomService roomService, WorkshopReservationRepository workShopReservationRepository, ActivityTypeRepository activityTypeRepository, AttendanceRepository attendanceRepository) {
        this.activityRepository = activityRepository;
        this.congressService = congressService;
        this.roomService = roomService;
        this.workShopReservationRepository = workShopReservationRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.attendanceRepository = attendanceRepository;
    }


    @Override
    public ActivityResponse createActivity(Long congressId, CreateActivityRequest request) throws NotFoundException, BusinessRuleException {
        //Obtenemos el congreso para validar su existencia
        CongressEntity congress = congressService.findCongressEntityById(congressId);
        if(!congress.getIsActive()) {
            throw new BusinessRuleException("Not allowed to create activities for an inactive congress.");
        }
        // Validamos que la sala exista
        RoomEntity roomEntity = roomService.findRoomById(request.getRoomId());

        //Validamos que la fecha de la actividad este dentro de las fechas del congreso
        if(request.getStartTime().isBefore(congress.getStartDate().atStartOfDay()) || request.getEndTime().isAfter(congress.getEndDate().atTime(LocalTime.MAX))) {
            throw new BusinessRuleException("Activity times must be within the congress dates (" + congress.getStartDate() + " to " + congress.getEndDate() + ").");
        }

        //Validamos las fechas de la actividad
        if(request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessRuleException("The start time must be before the end time.");
        }

        // Validamos que no haya solapamientos de actividades en la misma sala
        if (activityRepository
                .existsByRoom_IdRoomAndStartTimeLessThanAndEndTimeGreaterThan(
                        roomEntity.getIdRoom(),
                        request.getEndTime(),
                        request.getStartTime()
                )) {
            throw new BusinessRuleException(
                    "There is already an activity scheduled in this room during the specified time."
            );
        }

        ActivityTypeEntity activityType = activityTypeRepository
                .findById(request.getActivityTypeId()).orElseThrow(() -> new NotFoundException("Activity type not found"));

        if (TALLER.equalsIgnoreCase(activityType.getTypeName())) {
            if (request.getMaxCapacity() == null || request.getMaxCapacity() <= 0) {
                throw new BusinessRuleException("Workshops must have a maximum capacity greater than 0");
            }
        }

        //Creamos la actividad
        ActivityEntity activity = request.toEntity(congress,roomEntity, activityType);
        ActivityEntity savedActivity = activityRepository.save(activity);

        return ActivityResponse.fromEntity(savedActivity);
    }

    @Override
    public ActivityResponse updateActivity(Long activityId, UpdateActivityRequest request) throws NotFoundException, BusinessRuleException {
        //Obtenemos la actividad para validar su existencia
        ActivityEntity existingActivity = getActivityById(activityId);

        // Validamos las fechas de la actividad
        if(request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessRuleException("The start time must be before the end time.");
        }

        // Validamos que no haya solapamientos de actividades en la misma sala, excluyendo la actividad actual
        if(activityRepository.existsByRoom_IdRoomAndStartTimeLessThanAndEndTimeGreaterThanAndIdActivityNot(
                existingActivity.getRoom().getIdRoom(),
                request.getEndTime(),
                request.getStartTime(),
                activityId
        )){
            throw new BusinessRuleException("There is already an activity scheduled in this room during the specified time.");
        }

        //Si la actividad es de tipo taller verificar que la capacidad maxima no sea menor en caso de que hayan reservas
        if (TALLER.equalsIgnoreCase(existingActivity.getActivityType().getTypeName())) {

            if (request.getMaxCapacity() == null || request.getMaxCapacity() <= 0) {
                throw new BusinessRuleException("Workshops must define a valid maximum capacity");
            }

            int existingReservations = workShopReservationRepository
                    .countByActivity_IdActivity(activityId);

            if (request.getMaxCapacity() < existingReservations) {
                throw new BusinessRuleException(
                        "The maximum capacity cannot be less than existing reservations ("
                                + existingReservations + ")."
                );
            }
        }

        existingActivity.setActivityName(request.getActivityName());
        existingActivity.setDescription(request.getDescription());
        existingActivity.setStartTime(request.getStartTime());
        existingActivity.setEndTime(request.getEndTime());
        existingActivity.setMaxCapacity(request.getMaxCapacity());

        ActivityEntity updatedActivity = activityRepository.save(existingActivity);
        return ActivityResponse.fromEntity(updatedActivity);
    }

    @Override
    public void deleteActivity(Long activityId) throws NotFoundException, BusinessRuleException {
        //Obtenemos la actividad para validar su existencia
        ActivityEntity activity = getActivityById(activityId);

        //Validamos que no existe ninguna asistencia
        if (attendanceRepository.existsByActivity_IdActivity(activityId)) {
            throw new BusinessRuleException("Cannot delete activity with associated attendances");
        }


        //Validamos que la actividad no tenga reservas asociadas
        if (workShopReservationRepository.existsByActivity_IdActivity(activityId)) {
            throw new BusinessRuleException("Cannot delete activity with associated reservations");
        }

        //Eliminamos la actividad
        activityRepository.delete(activity);


    }

    @Override
    public ActivityEntity getActivityById(Long activityId) throws NotFoundException {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found" ));
    }

    @Override
    public List<ActivityResponse> getActivitiesByRoomId(Long roomId) throws NotFoundException {
        // Validamos que la sala exista
        roomService.findRoomById(roomId);

        List<ActivityEntity> activities = activityRepository.findByRoom_IdRoom(roomId);
        return activities.stream()
                .map(ActivityResponse::fromEntity)
                .toList();
    }

    @Override
    public List<ActivityResponse> getActivitiesByCongressId(Long congressId) throws NotFoundException {
        // Validamos que el congreso exista
        congressService.findCongressEntityById(congressId);

        List<ActivityEntity> activities = activityRepository.findByCongress_IdCongress(congressId);
        return activities.stream()
                .map(ActivityResponse::fromEntity)
                .toList();
    }
}
