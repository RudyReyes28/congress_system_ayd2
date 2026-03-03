package com.alessandro.congress_management.services.activitypresenter;

import com.alessandro.congress_management.dto.activitypresenter.*;
import com.alessandro.congress_management.dto.user_manager.CreateUserCommand;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;
import com.alessandro.congress_management.repositories.activitypresenter.ActivityPresenterRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.services.activity.ActivityService;
import com.alessandro.congress_management.services.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityPresenterServiceImpl implements ActivityPresenterService {
    private final ActivityPresenterRepository activityPresenterRepository;
    private final ActivityService activityService;
    private final UserService userService;
    private final RegistrationRepository registrationRepository;

    public ActivityPresenterServiceImpl(ActivityPresenterRepository activityPresenterRepository, ActivityService activityService, UserService userService, RegistrationRepository registrationRepository) {
        this.activityPresenterRepository = activityPresenterRepository;
        this.activityService = activityService;
        this.userService = userService;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public ActivityPresenterResponse assignExistingUser(ActivityPresenterRequest request, Long idActivity) throws NotFoundException, BusinessRuleException {
        //Obtener la actividad
        ActivityEntity activity = activityService.getActivityById(idActivity);
        //Verificar que el congreso este activo
        if (!activity.getCongress().getIsActive()) {
            throw new BusinessRuleException("Congress is not active. Cannot assign presenters to activities of an inactive congress.");
        }
        //Obtener el usuario
        UserEntity user = userService.getUserById(request.getIdUser());
        //Validar que el usuario este activo
        if (!user.getIsActive()) {
            throw new BusinessRuleException("User is not active. Cannot be assigned as presenter.");
        }
        //Validar que el usuario esté registrado en el congreso o que no sea un usuario invitado (si es invitado no es necesario que esté registrado en el congreso)
        if (registrationRepository.existsByUser_IdUserAndCongress_IdCongress(request.getIdUser(), activity.getCongress().getIdCongress()) && !request.getInvitedSpeaker()) {
            throw new BusinessRuleException("User is not registered for the congress. Only registered users can be assigned as presenters unless they are invited speakers.");
        }

        //Validar que el usuario no sea ya presentador de la actividad
        if (activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(idActivity, request.getIdUser())) {
            throw new BusinessRuleException("User is already assigned as presenter for this activity");
        }

        //Si es el primer presentador asignado, marcarlo como autor principal
        boolean isFirstPresenter = activityPresenterRepository.countByActivity_IdActivity(idActivity) == 0;

        //Crear la asignacion
        ActivityPresenterEntity presenter = new ActivityPresenterEntity();
        presenter.setActivity(activity);
        presenter.setUser(user);
        presenter.setIsInvitedSpeaker(request.getInvitedSpeaker());
        presenter.setIsMainAuthor(isFirstPresenter);

        //Guardar la asignacion
        ActivityPresenterEntity savedPresenter = activityPresenterRepository.save(presenter);

        return ActivityPresenterResponse.fromEntity(savedPresenter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityPresenterResponse assignInvitedUser(AssignInviteUserActivityRequest request, Long idActivity) throws NotFoundException, BusinessRuleException, DuplicatedEntityException {
        CreateUserCommand command = new CreateUserCommand(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getPhoneNumber(),
                request.getOrganization(),
                request.getIdentificationNumber(),
                "PARTICIPANT"
        );

        UserEntity user = userService.createUser(command);

        ActivityPresenterRequest presenterRequest = new ActivityPresenterRequest(user.getIdUser(), true);
        return assignExistingUser(presenterRequest, idActivity);
    }

    @Override
    public void removePresenter(Long idActivityPresenter) throws NotFoundException, BusinessRuleException {
        //Obtener la asignacion
        ActivityPresenterEntity presenter = findById(idActivityPresenter);
        //Verificar que el presentador no sea el autor principal
        if (presenter.getIsMainAuthor()) {
            throw new BusinessRuleException("Cannot remove the main author. Please assign another presenter as the main author before removing this presenter.");
        }
        //Eliminar la asignacion
        activityPresenterRepository.delete(presenter);
    }

    @Override
    public List<DetailActivityPresenterResponse> getPresentersByActivity(Long idActivity) {
        List<ActivityPresenterEntity> presenters = activityPresenterRepository.findByActivity_IdActivity(idActivity);
        return presenters.stream()
                .map(DetailActivityPresenterResponse::fromEntity)
                .toList();
    }

    @Override
    public ActivityPresenterEntity findById(Long idActivityPresenter) throws NotFoundException {
        return activityPresenterRepository.findById(idActivityPresenter)
                .orElseThrow(() -> new NotFoundException("Activity presenter with id " + idActivityPresenter + " not found"));
    }

    @Override
    public List<EligibleUsersActivityResponse> getEligibleRegisteredUsers(Long idActivity) throws NotFoundException {
        //Obtener la actividad
        ActivityEntity activity = activityService.getActivityById(idActivity);
        //Obtener los usuarios registrados en el congreso
        List<UserEntity> registeredUsers = registrationRepository.findActiveUsersByCongressId(activity.getCongress().getIdCongress());
        //Filtrar los usuarios que no son presentadores de la actividad
        List<UserEntity> eligibleUsers = registeredUsers.stream()
                .filter(user -> !activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(idActivity, user.getIdUser()))
                .toList();
        //Mapear a DTO
        return eligibleUsers.stream()
                .map(EligibleUsersActivityResponse::fromEntity)
                .toList();

    }

    @Override
    public List<EligibleUsersActivityResponse> getEligibleInvitedUsers(Long activityId) throws NotFoundException {
        //Obtener la actividad
        ActivityEntity activity = activityService.getActivityById(activityId);
        //Obtener los usuarios invitados que no están registrados en el congreso (todos los usuarios que no están registrados en el congreso son elegibles para ser invitados)
        List<UserEntity> invitedUsers = userService.getAllUsers().stream()
                .filter(user -> registrationRepository.existsByUser_IdUserAndCongress_IdCongress(user.getIdUser(), activity.getCongress().getIdCongress()))
                //Filtrar los usuarios que no son presentadores de la actividad
                .filter(user -> !activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(activityId, user.getIdUser()))
                .toList();
        //Mapear a DTO
        return invitedUsers.stream()
                .map(EligibleUsersActivityResponse::fromEntity)
                .toList();
    }

}
