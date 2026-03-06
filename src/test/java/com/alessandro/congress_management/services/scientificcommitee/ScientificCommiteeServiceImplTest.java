package com.alessandro.congress_management.services.scientificcommitee;

import com.alessandro.congress_management.dto.scientificcommitee.EligibleUserCommitteeResponse;
import com.alessandro.congress_management.dto.scientificcommitee.ScientificCommiteeResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.congress_management.ScientificCommiteeEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.repositories.scientificcommitee.ScientificCommiteeRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
public class ScientificCommiteeServiceImplTest {

    @Mock
    private ScientificCommiteeRepository scientificCommiteeRepository;

    @Mock
    private CongressService congressService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ScientificCommiteeServiceImpl scientificCommiteeService;

    //--------------- TESTS FOR CREATE SCIENTIFIC COMMITTEE MEMBER ---------------

    @Test
    public void testCreateScientificCommiteeMember_Success() throws NotFoundException, BusinessRuleException {
        //Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress");
        UserEntity user = createUser(userId, "testuser");
        ScientificCommiteeEntity committeeMember = createScientificCommiteeEntity(congressId, userId);

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(userService.getUserById(userId)).thenReturn(user);
        when(scientificCommiteeRepository.existsByCongress_IdCongressAndUser_IdUser(congressId, userId)).thenReturn(false);
        when(scientificCommiteeRepository.save(any(ScientificCommiteeEntity.class)))
                .thenAnswer(invocation -> {
                    ScientificCommiteeEntity savedEntity = invocation.getArgument(0);
                    savedEntity.setIdCommitteeMember(1L);
                    return savedEntity;
                });

        ArgumentCaptor<ScientificCommiteeEntity> committeeMemberCaptor = ArgumentCaptor.forClass(ScientificCommiteeEntity.class);



        //Act
        ScientificCommiteeResponse response = scientificCommiteeService.createScientificCommiteeMember(congressId, userId);

        //Assert
        verify(scientificCommiteeRepository).save(committeeMemberCaptor.capture());
        ScientificCommiteeEntity savedCommitteeMember = committeeMemberCaptor.getValue();

        assertAll(
                () -> assertEquals(congressId, savedCommitteeMember.getCongress().getIdCongress()),
                () -> assertEquals(userId, savedCommitteeMember.getUser().getIdUser())
        );

    }

    @Test
    public void testCreateScientificCommiteeMember_InactiveCongress() throws NotFoundException {
        //Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress");
        congress.setIsActive(false);

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);

        //Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> scientificCommiteeService.createScientificCommiteeMember(congressId, userId));
        assertEquals("Cannot assign a scientific committee member to an inactive congress", exception.getMessage());

        verify(scientificCommiteeRepository, never()).save(any(ScientificCommiteeEntity.class));

    }

    @Test
    public void testCreateScientificCommiteeMember_InactiveUser() throws NotFoundException {
        //Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress");
        UserEntity user = createUser(userId, "testuser");
        user.setIsActive(false);

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(userService.getUserById(userId)).thenReturn(user);

        //Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> scientificCommiteeService.createScientificCommiteeMember(congressId, userId));
        assertEquals("Cannot assign an inactive user as a scientific committee member", exception.getMessage());

        verify(scientificCommiteeRepository, never()).save(any(ScientificCommiteeEntity.class));

    }

    @Test
    public void testCreateScientificCommiteeMember_UserAlreadyMember() throws NotFoundException {
        //Arrange
        Long congressId = 1L;
        Long userId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress");
        UserEntity user = createUser(userId, "testuser");

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(userService.getUserById(userId)).thenReturn(user);
        when(scientificCommiteeRepository.existsByCongress_IdCongressAndUser_IdUser(congressId, userId)).thenReturn(true);

        //Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> scientificCommiteeService.createScientificCommiteeMember(congressId, userId));
        assertEquals("The user is already a member of the scientific committee of this congress", exception.getMessage());

        verify(scientificCommiteeRepository, never()).save(any(ScientificCommiteeEntity.class));
    }

    //--------------- TESTS FOR GET SCIENTIFIC COMMITTEE MEMBERS BY CONGRESS ID ---------------
    @Test
    public void testGetScientificCommiteeMembersByCongressId_Success() {
        //Arrange
        Long congressId = 1L;
        ScientificCommiteeEntity committeeMember1 = createScientificCommiteeEntity(congressId, 1L);
        ScientificCommiteeEntity committeeMember2 = createScientificCommiteeEntity(congressId, 2L);

        when(scientificCommiteeRepository.findAllByCongress_IdCongress(congressId))
                .thenReturn(List.of(committeeMember1, committeeMember2));

        //Act
        List<ScientificCommiteeResponse> responses = scientificCommiteeService.getScientificCommiteeMembersByCongressId(congressId);

        //Assert
        assertEquals(2, responses.size());
        assertEquals(committeeMember1.getUser().getUsername(), responses.get(0).getUsername());
        assertEquals(committeeMember2.getUser().getUsername(), responses.get(1).getUsername());
    }

    @Test
    public void testGetScientificCommiteeMembersByCongressId_NoMembers() {
        //Arrange
        Long congressId = 1L;

        when(scientificCommiteeRepository.findAllByCongress_IdCongress(congressId))
                .thenReturn(List.of());

        //Act
        List<ScientificCommiteeResponse> responses = scientificCommiteeService.getScientificCommiteeMembersByCongressId(congressId);

        //Assert
        assertTrue(responses.isEmpty());
    }

    //--------------- TESTS FOR DELETE SCIENTIFIC COMMITTEE MEMBER ---------------
    @Test
    public void testDeleteScientificCommiteeMember_Success() throws NotFoundException {
        //Arrange
        Long congressId = 1L;
        Long userId = 1L;
        ScientificCommiteeEntity committeeMember = createScientificCommiteeEntity(congressId, userId);

        when(scientificCommiteeRepository.findByCongress_IdCongressAndUser_IdUser(congressId, userId))
                .thenReturn(Optional.of(committeeMember));

        //Act
        scientificCommiteeService.deleteScientificCommiteeMember(congressId, userId);

        //Assert
        verify(scientificCommiteeRepository).delete(committeeMember);
    }

    @Test
    public void testDeleteScientificCommiteeMember_NotFound() {
        //Arrange
        Long congressId = 1L;
        Long userId = 1L;

        when(scientificCommiteeRepository.findByCongress_IdCongressAndUser_IdUser(congressId, userId))
                .thenReturn(Optional.empty());

        //Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> scientificCommiteeService.deleteScientificCommiteeMember(congressId, userId));
        assertEquals("Scientific committee member not found", exception.getMessage());
        verify(scientificCommiteeRepository, never()).delete(any(ScientificCommiteeEntity.class));
    }

    //--------------- TESTS FOR GET ELIGIBLE USERS FOR COMMITTEE ---------------
    @Test
    public void testGetEligibleUsersForCommittee_Success() throws NotFoundException {
        //Arrange
        Long congressId = 1L;
        CongressEntity congress = createCongress(congressId, "Test Congress");
        UserEntity eligibleUser1 = createUser(1L, "eligibleuser1");
        UserEntity eligibleUser2 = createUser(2L, "eligibleuser2");

        when(congressService.findCongressEntityById(congressId)).thenReturn(congress);
        when(userService.findActiveUsers()).thenReturn(List.of(eligibleUser1, eligibleUser2));

        //Act
        List<EligibleUserCommitteeResponse> responses = scientificCommiteeService.getEligibleUsersForCommittee(congressId);

        //Assert
        assertEquals(2, responses.size());
        assertEquals("Test User eligibleuser1", responses.get(0).getFullName());
        assertEquals("Test User eligibleuser2", responses.get(1).getFullName());
    }

    @Test
    public void testGetEligibleUsersForCommittee_CongressNotFound() throws NotFoundException {
        //Arrange
        Long congressId = 1L;

        when(congressService.findCongressEntityById(congressId)).thenThrow(new NotFoundException("Congress not found"));

        //Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> scientificCommiteeService.getEligibleUsersForCommittee(congressId));
        assertEquals("Congress not found", exception.getMessage());
        verify(userService, never()).findActiveUsers();
    }

    // ----------- HELPER METHODS ------------------

    private UserEntity createUser(Long id, String username) {
        UserEntity user = new UserEntity();
        user.setIdUser(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFullName("Test User " + username);
        user.setIsActive(true);
        return user;
    }

    private CongressEntity createCongress(Long id, String name) {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(id);
        congress.setCongressName(name);
        congress.setDescription("Description for " + name);
        congress.setStartDate(LocalDate.of(2026, 5, 15));
        congress.setEndDate(LocalDate.of(2026, 5, 17));
        congress.setLocation("Guatemala City");
        congress.setPrice(new BigDecimal("150.00"));
        congress.setIsActive(true);

        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(1L);
        institution.setInstitutionName("USAC");
        congress.setInstitution(institution);

        return congress;
    }

    private ScientificCommiteeEntity createScientificCommiteeEntity(Long congressId, Long userId) {
        ScientificCommiteeEntity committeeMember = new ScientificCommiteeEntity();
        committeeMember.setCongress(createCongress(congressId, "Test Congress"));
        committeeMember.setUser(createUser(userId, "testuser"));
        return committeeMember;
    }


}
