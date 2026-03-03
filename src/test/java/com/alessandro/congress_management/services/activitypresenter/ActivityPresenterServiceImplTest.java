package com.alessandro.congress_management.services.activitypresenter;

import com.alessandro.congress_management.dto.activitypresenter.*;
import com.alessandro.congress_management.dto.user_manager.CreateUserCommand;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityPresenterEntity;
import com.alessandro.congress_management.repositories.activitypresenter.ActivityPresenterRepository;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.services.activity.ActivityService;
import com.alessandro.congress_management.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityPresenterServiceImplTest {

    @Mock
    private ActivityPresenterRepository activityPresenterRepository;

    @Mock
    private ActivityService activityService;

    @Mock
    private UserService userService;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private ActivityPresenterServiceImpl service;

    // ─── Fixtures ───────────────────────────────────────────────────────────────

    private CongressEntity activeCongress;
    private CongressEntity inactiveCongress;
    private ActivityEntity activity;
    private ActivityEntity activityInInactiveCongress;
    private UserEntity activeUser;
    private UserEntity inactiveUser;

    @BeforeEach
    void setUp() {
        service = new ActivityPresenterServiceImpl(
                activityPresenterRepository, activityService, userService, registrationRepository
        );

        activeCongress = new CongressEntity();
        activeCongress.setIdCongress(1L);
        activeCongress.setIsActive(true);

        inactiveCongress = new CongressEntity();
        inactiveCongress.setIdCongress(2L);
        inactiveCongress.setIsActive(false);

        activity = new ActivityEntity();
        activity.setIdActivity(10L);
        activity.setActivityName("Introduction to AI");
        activity.setCongress(activeCongress);

        activityInInactiveCongress = new ActivityEntity();
        activityInInactiveCongress.setIdActivity(11L);
        activityInInactiveCongress.setActivityName("Old Activity");
        activityInInactiveCongress.setCongress(inactiveCongress);

        activeUser = new UserEntity();
        activeUser.setIdUser(100L);
        activeUser.setFullName("Ana López");
        activeUser.setEmail("ana@mail.com");
        activeUser.setOrganization("USAC");
        activeUser.setIdentificationNumber("1234567");
        activeUser.setIsActive(true);

        inactiveUser = new UserEntity();
        inactiveUser.setIdUser(101L);
        inactiveUser.setFullName("Carlos Pérez");
        inactiveUser.setIsActive(false);
    }

    //--------------------- TESTS ASSIGN EXISTING USER ---------------------
    @Test
    void success_firstPresenter_isMarkedAsMainAuthor() throws Exception {
        //Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(100L, false);

        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getUserById(100L)).thenReturn(activeUser);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(100L, 1L)).thenReturn(false);
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(false);
        when(activityPresenterRepository.countByActivity_IdActivity(10L)).thenReturn(0);
        when(activityPresenterRepository.save(any())).thenAnswer(inv -> {
            ActivityPresenterEntity e = inv.getArgument(0);
            e.setIdActivityPresenter(1L);
            return e;
        });

        //Act
        ActivityPresenterResponse response = service.assignExistingUser(request, 10L);

        //Assert
        assertThat(response.getMainAuthor()).isTrue();
        assertThat(response.getInvitedSpeaker()).isFalse();
        assertThat(response.getPresenterName()).isEqualTo("Ana López");
        assertThat(response.getActivityName()).isEqualTo("Introduction to AI");
    }

    @Test
    void success_notFirstPresenter_isNotMainAuthor() throws Exception {
        //Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(100L, false);

        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getUserById(100L)).thenReturn(activeUser);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(100L, 1L)).thenReturn(false);
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(false);
        when(activityPresenterRepository.countByActivity_IdActivity(10L)).thenReturn(2);
        when(activityPresenterRepository.save(any())).thenAnswer(inv -> {
            ActivityPresenterEntity e = inv.getArgument(0);
            e.setIdActivityPresenter(2L);
            return e;
        });

        //Act
        ActivityPresenterResponse response = service.assignExistingUser(request, 10L);

        //Assert
        assertThat(response.getMainAuthor()).isFalse();
    }

    @Test
    void shouldPersistCorrectFields() throws Exception {
        //Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(100L, true);

        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getUserById(100L)).thenReturn(activeUser);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(100L, 1L)).thenReturn(false);
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(false);
        when(activityPresenterRepository.countByActivity_IdActivity(10L)).thenReturn(0);
        when(activityPresenterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //Act
        service.assignExistingUser(request, 10L);

        //Assert
        ArgumentCaptor<ActivityPresenterEntity> captor = ArgumentCaptor.forClass(ActivityPresenterEntity.class);
        verify(activityPresenterRepository).save(captor.capture());

        ActivityPresenterEntity saved = captor.getValue();
        assertThat(saved.getActivity()).isEqualTo(activity);
        assertThat(saved.getUser()).isEqualTo(activeUser);
        assertThat(saved.getIsInvitedSpeaker()).isTrue();
        assertThat(saved.getIsMainAuthor()).isTrue();
    }

    @Test
    void inactiveCongress_throws() throws Exception {
        // Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(100L, false);
        when(activityService.getActivityById(11L)).thenReturn(activityInInactiveCongress);

        //Act & Assert
        assertThatThrownBy(() -> service.assignExistingUser(request, 11L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void inactiveUser_throws() throws Exception {
        //Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(101L, false);
        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getUserById(101L)).thenReturn(inactiveUser);

        //Act & Assert
        assertThatThrownBy(() -> service.assignExistingUser(request, 10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void registeredUserNotInvited_throws() throws Exception {
        //Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(100L, false);
        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getUserById(100L)).thenReturn(activeUser);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(100L, 1L)).thenReturn(true);

        //Act & Assert
        assertThatThrownBy(() -> service.assignExistingUser(request, 10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not registered");
    }

    @Test
    void registeredUserAsInvitedSpeaker_doesNotThrow() throws Exception {
        //Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(100L, true);
        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getUserById(100L)).thenReturn(activeUser);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(100L, 1L)).thenReturn(true);
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(false);
        when(activityPresenterRepository.countByActivity_IdActivity(10L)).thenReturn(0);
        when(activityPresenterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //Act & Assert
        assertThatNoException().isThrownBy(() -> service.assignExistingUser(request, 10L));
    }

    @Test
    void alreadyPresenter_throws() throws Exception {
        //Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(100L, false);
        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getUserById(100L)).thenReturn(activeUser);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(100L, 1L)).thenReturn(false);
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(true);

        //Act & Assert
        assertThatThrownBy(() -> service.assignExistingUser(request, 10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already assigned");
    }

    @Test
    void activityNotFound_throws() throws Exception {
        //Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(100L, false);
        when(activityService.getActivityById(99L)).thenThrow(new NotFoundException("Activity not found"));

        //Act & Assert
        assertThatThrownBy(() -> service.assignExistingUser(request, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("should throw NotFoundException when user does not exist")
    void userNotFound_throws() throws Exception {
        // Arrange
        ActivityPresenterRequest request = new ActivityPresenterRequest(999L, false);
        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getUserById(999L)).thenThrow(new NotFoundException("User not found"));

        // Act &  Assert
        assertThatThrownBy(() -> service.assignExistingUser(request, 10L))
                .isInstanceOf(NotFoundException.class);
    }

   //--------------- TEST ASSIGN INVITED USER ----------------
    @Nested
    class AssignInvitedUser {

        private AssignInviteUserActivityRequest inviteRequest;

        @BeforeEach
        void setUpRequest() {
            inviteRequest = new AssignInviteUserActivityRequest(
                    "invited@mail.com", "Guest Speaker", "55551234",
                    "External Org", "guestspeaker", "password123", "EXT-001"
            );
        }

        @Test
        void success_createsUserAndAssigns() throws Exception {

            when(userService.createUser(any(CreateUserCommand.class))).thenReturn(activeUser);
            when(activityService.getActivityById(10L)).thenReturn(activity);
            when(userService.getUserById(activeUser.getIdUser())).thenReturn(activeUser);
            when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(activeUser.getIdUser(), 1L)).thenReturn(false);
            when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, activeUser.getIdUser())).thenReturn(false);
            when(activityPresenterRepository.countByActivity_IdActivity(10L)).thenReturn(0);
            when(activityPresenterRepository.save(any())).thenAnswer(inv -> {
                ActivityPresenterEntity e = inv.getArgument(0);
                e.setIdActivityPresenter(5L);
                return e;
            });

            ActivityPresenterResponse response = service.assignInvitedUser(inviteRequest, 10L);

            assertThat(response.getInvitedSpeaker()).isTrue();
            assertThat(response.getMainAuthor()).isTrue();
        }

        @Test
        void shouldCreateUserWithParticipantRole() throws Exception {
            when(userService.createUser(any(CreateUserCommand.class))).thenReturn(activeUser);
            when(activityService.getActivityById(10L)).thenReturn(activity);
            when(userService.getUserById(activeUser.getIdUser())).thenReturn(activeUser);
            when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(any(), any())).thenReturn(false);
            when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(any(), any())).thenReturn(false);
            when(activityPresenterRepository.countByActivity_IdActivity(10L)).thenReturn(0);
            when(activityPresenterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.assignInvitedUser(inviteRequest, 10L);

            ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
            verify(userService).createUser(captor.capture());
            assertThat(captor.getValue().roleName()).isEqualTo("PARTICIPANT");
        }

        @Test
        void duplicateUser_throws() throws Exception {
            when(userService.createUser(any())).thenThrow(new DuplicatedEntityException("User already exists"));

            assertThatThrownBy(() -> service.assignInvitedUser(inviteRequest, 10L))
                    .isInstanceOf(DuplicatedEntityException.class);

            verify(activityPresenterRepository, never()).save(any());
        }

        @Test
        void rollbackIfAssignFails() throws Exception {
            when(userService.createUser(any())).thenReturn(activeUser);
            when(activityService.getActivityById(10L)).thenReturn(activityInInactiveCongress);
            // activityInInactiveCongress has inactive congress → BusinessRuleException

            assertThatThrownBy(() -> service.assignInvitedUser(inviteRequest, 10L))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    //--------------------- TEST REMOVE PRESENTER ---------------------

    @Test
    void success_removesNonMainAuthor() throws Exception {
        ActivityPresenterEntity presenter = buildPresenter(1L, false);
        when(activityPresenterRepository.findById(1L)).thenReturn(Optional.of(presenter));

        service.removePresenter(1L);

        verify(activityPresenterRepository).delete(presenter);
    }

    @Test
    void mainAuthor_throws() throws Exception {
        ActivityPresenterEntity mainAuthor = buildPresenter(1L, true);
        when(activityPresenterRepository.findById(1L)).thenReturn(Optional.of(mainAuthor));

        assertThatThrownBy(() -> service.removePresenter(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("main author");

        verify(activityPresenterRepository, never()).delete(any());
    }

    @Test
    void notFound_throws() {
        when(activityPresenterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removePresenter(99L))
                .isInstanceOf(NotFoundException.class);
    }

    //--------------------- TEST GET PRESENTERS BY ACTIVITY -------------------
    @Test
    void returnsMappedList() {
        ActivityPresenterEntity p1 = buildFullPresenter(1L, activeUser, activity, false, true);
        ActivityPresenterEntity p2 = buildFullPresenter(2L, activeUser, activity, true, false);
        when(activityPresenterRepository.findByActivity_IdActivity(10L)).thenReturn(List.of(p1, p2));

        List<DetailActivityPresenterResponse> result = service.getPresentersByActivity(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIdActivityPresenter()).isEqualTo(1L);
        assertThat(result.get(1).getInvitedSpeaker()).isTrue();
    }

    @Test
    void emptyList() {
        when(activityPresenterRepository.findByActivity_IdActivity(10L)).thenReturn(List.of());

        List<DetailActivityPresenterResponse> result = service.getPresentersByActivity(10L);

        assertThat(result).isEmpty();
    }

    //--------------------- TEST FIND BY ID -------------------
    @Test
    void found() throws Exception {
        ActivityPresenterEntity presenter = buildPresenter(1L, false);
        when(activityPresenterRepository.findById(1L)).thenReturn(Optional.of(presenter));

        ActivityPresenterEntity result = service.findById(1L);

        assertThat(result).isEqualTo(presenter);
    }

    @Test
    void notFound_throws_getById() {
        when(activityPresenterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------- TESTS GET ELIGIBLE REGISTERED USERS ----------------
    @Test
    void returnsEligibleUsers() throws Exception {
        UserEntity alreadyPresenter = new UserEntity();
        alreadyPresenter.setIdUser(200L);

        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(registrationRepository.findActiveUsersByCongressId(1L))
                .thenReturn(List.of(activeUser, alreadyPresenter));
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(false);
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 200L)).thenReturn(true);

        List<EligibleUsersActivityResponse> result = service.getEligibleRegisteredUsers(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdUser()).isEqualTo(100L);
    }

    @Test
    void allAlreadyPresenters_returnsEmpty() throws Exception {
        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(registrationRepository.findActiveUsersByCongressId(1L)).thenReturn(List.of(activeUser));
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(true);

        List<EligibleUsersActivityResponse> result = service.getEligibleRegisteredUsers(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void activityNotFound_eligibleUsers_throws() throws Exception {
        when(activityService.getActivityById(99L)).thenThrow(new NotFoundException("Activity not found"));

        assertThatThrownBy(() -> service.getEligibleRegisteredUsers(99L))
                .isInstanceOf(NotFoundException.class);
    }

    //--------------------- TESTS GET ELIGIBLE INVITED USERS ----------------
    @Test
    void returnsEligibleInvitedUsers() throws Exception {
        UserEntity notRegistered = new UserEntity();
        notRegistered.setIdUser(300L);
        notRegistered.setFullName("External Guest");
        notRegistered.setEmail("guest@ext.com");
        notRegistered.setOrganization("External");
        notRegistered.setIdentificationNumber("EXT-999");

        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getAllUsers()).thenReturn(List.of(activeUser, notRegistered));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(100L, 1L)).thenReturn(true);
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(300L, 1L)).thenReturn(false);
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(false);

        List<EligibleUsersActivityResponse> result = service.getEligibleInvitedUsers(10L);


        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdUser()).isEqualTo(100L);
    }

    @Test
    void alreadyPresenter_excluded() throws Exception {
        when(activityService.getActivityById(10L)).thenReturn(activity);
        when(userService.getAllUsers()).thenReturn(List.of(activeUser));
        when(registrationRepository.existsByUser_IdUserAndCongress_IdCongress(100L, 1L)).thenReturn(true);
        when(activityPresenterRepository.existsByActivity_IdActivityAndUser_IdUser(10L, 100L)).thenReturn(true);

        List<EligibleUsersActivityResponse> result = service.getEligibleInvitedUsers(10L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should throw NotFoundException when activity does not exist")
    void activityNotFound_invitedUser_throws() throws Exception {
        when(activityService.getActivityById(99L)).thenThrow(new NotFoundException("Activity not found"));

        assertThatThrownBy(() -> service.getEligibleInvitedUsers(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------------- HELPER METHODS ----------------

    private ActivityPresenterEntity buildPresenter(Long id, boolean mainAuthor) {
        ActivityPresenterEntity e = new ActivityPresenterEntity();
        e.setIdActivityPresenter(id);
        e.setActivity(activity);
        e.setUser(activeUser);
        e.setIsInvitedSpeaker(false);
        e.setIsMainAuthor(mainAuthor);
        return e;
    }

    private ActivityPresenterEntity buildFullPresenter(Long id, UserEntity user,
                                                       ActivityEntity act,
                                                       boolean invited, boolean main) {
        ActivityPresenterEntity e = new ActivityPresenterEntity();
        e.setIdActivityPresenter(id);
        e.setActivity(act);
        e.setUser(user);
        e.setIsInvitedSpeaker(invited);
        e.setIsMainAuthor(main);
        return e;
    }
}