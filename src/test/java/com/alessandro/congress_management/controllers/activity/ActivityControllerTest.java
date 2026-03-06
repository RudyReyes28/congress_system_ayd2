package com.alessandro.congress_management.controllers.activity;

import com.alessandro.congress_management.controllers.room.RoomController;
import com.alessandro.congress_management.dto.activity.ActivityResponse;
import com.alessandro.congress_management.dto.activity.CreateActivityRequest;
import com.alessandro.congress_management.dto.activity.UpdateActivityRequest;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityEntity;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.activity.ActivityService;
import com.alessandro.congress_management.services.room.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(ActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    //---------------------------TESTS FOR CREATE ACTIVITY------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testCreateActivity_Success() throws Exception {
        Long congressId = 1L;
        Long roomId = 1L;
        Integer activityTypeId = 1; // TALLER

        CreateActivityRequest request = createValidCreateActivityRequest(roomId, activityTypeId);
        ActivityResponse expectedResponse = createExpectedActivityResponse(1L, "Main Hall");

        when(activityService.createActivity(eq(congressId), any(CreateActivityRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/congresses/{congressId}/activities", congressId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idActivity").value(expectedResponse.getIdActivity()))
                .andExpect(jsonPath("$.activityName").value(expectedResponse.getActivityName()))
                .andExpect(jsonPath("$.description").value(expectedResponse.getDescription()))
                .andExpect(jsonPath("$.activityType").value(expectedResponse.getActivityType()))
                .andExpect(jsonPath("$.startTime").value(expectedResponse.getStartTime()))
                .andExpect(jsonPath("$.endTime").value(expectedResponse.getEndTime()))
                .andExpect(jsonPath("$.roomName").value(expectedResponse.getRoomName()))
                .andExpect(jsonPath("$.maxCapacity").value(expectedResponse.getMaxCapacity()));

        verify(activityService).createActivity(eq(congressId), any(CreateActivityRequest.class));
    }

    //------------ TESTS FOR UPDATE ACTIVITY------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testUpdateActivity_Success() throws Exception {
        // Arrange
        Long activityId = 1L;
        Integer newMaxCapacity = 30;
        UpdateActivityRequest request = createValidUpdateActivityRequest(newMaxCapacity);
        ActivityResponse expectedResponse = createExpectedActivityResponse(activityId, "Main Hall");

        when(activityService.updateActivity(eq(activityId), any(UpdateActivityRequest.class)))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/congresses/activities/{activityId}", activityId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idActivity").value(expectedResponse.getIdActivity()))
                .andExpect(jsonPath("$.activityName").value(expectedResponse.getActivityName()))
                .andExpect(jsonPath("$.description").value(expectedResponse.getDescription()))
                .andExpect(jsonPath("$.activityType").value(expectedResponse.getActivityType()))
                .andExpect(jsonPath("$.startTime").value(expectedResponse.getStartTime()))
                .andExpect(jsonPath("$.endTime").value(expectedResponse.getEndTime()))
                .andExpect(jsonPath("$.roomName").value(expectedResponse.getRoomName()))
                .andExpect(jsonPath("$.maxCapacity").value(expectedResponse.getMaxCapacity()));
        verify(activityService).updateActivity(eq(activityId), any(UpdateActivityRequest.class));
    }

    //-------------------------TESTS FOR DELETE ACTIVITY------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testDeleteActivity_Success() throws Exception {
        Long activityId = 1L;

        doNothing().when(activityService).deleteActivity(activityId);

        mockMvc.perform(delete("/api/v1/congresses/activities/{activityId}", activityId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(activityService).deleteActivity(activityId);
    }

    //-------------------------TESTS FOR GET ACTIVITIES BY ROOM ID------------------
    @Test
    @WithMockUser(roles = "PARTICIPANT")
    public void testGetActivitiesByRoomId_Success() throws Exception {
        // Arrange
        Long roomId = 1L;
        ActivityResponse activity1 = createExpectedActivityResponse(1L, "Main Hall");
        ActivityResponse activity2 = createExpectedActivityResponse(2L, "Main Hall");
        List<ActivityResponse> expectedActivities = Arrays.asList(activity1, activity2);

        when(activityService.getActivitiesByRoomId(roomId)).thenReturn(expectedActivities);

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses/rooms/{roomId}/activities", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expectedActivities.size()))
                .andExpect(jsonPath("$[0].idActivity").value(activity1.getIdActivity()))
                .andExpect(jsonPath("$[0].activityName").value(activity1.getActivityName()))
                .andExpect(jsonPath("$[0].description").value(activity1.getDescription()))
                .andExpect(jsonPath("$[0].activityType").value(activity1.getActivityType()))
                .andExpect(jsonPath("$[0].startTime").value(activity1.getStartTime()))
                .andExpect(jsonPath("$[0].endTime").value(activity1.getEndTime()))
                .andExpect(jsonPath("$[0].roomName").value(activity1.getRoomName()))
                .andExpect(jsonPath("$[0].maxCapacity").value(activity1.getMaxCapacity()))
                .andExpect(jsonPath("$[1].idActivity").value(activity2.getIdActivity()))
                .andExpect(jsonPath("$[1].activityName").value(activity2.getActivityName()))
                .andExpect(jsonPath("$[1].description").value(activity2.getDescription()))
                .andExpect(jsonPath("$[1].activityType").value(activity2.getActivityType()))
                .andExpect(jsonPath("$[1].startTime").value(activity2.getStartTime()))
                .andExpect(jsonPath("$[1].endTime").value(activity2.getEndTime()))
                .andExpect(jsonPath("$[1].roomName").value(activity2.getRoomName()))
                .andExpect(jsonPath("$[1].maxCapacity").value(activity2.getMaxCapacity()));

        verify(activityService).getActivitiesByRoomId(roomId);
    }

    //-------------- TESTS FOR GET ACTIVITY BY CONGRESS ID------------------
    @Test
    @WithMockUser(roles = "PARTICIPANT")
    public void testGetActivitiesByCongressId_Success() throws Exception {
        // Arrange
        Long congressId = 1L;
        ActivityResponse activity1 = createExpectedActivityResponse(1L, "Main Hall");
        ActivityResponse activity2 = createExpectedActivityResponse(2L, "Main Hall");
        List<ActivityResponse> expectedActivities = Arrays.asList(activity1, activity2);

        when(activityService.getActivitiesByCongressId(congressId)).thenReturn(expectedActivities);

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses/{congressId}/activities", congressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expectedActivities.size()))
                .andExpect(jsonPath("$[0].idActivity").value(activity1.getIdActivity()))
                .andExpect(jsonPath("$[0].activityName").value(activity1.getActivityName()))
                .andExpect(jsonPath("$[0].description").value(activity1.getDescription()))
                .andExpect(jsonPath("$[0].activityType").value(activity1.getActivityType()))
                .andExpect(jsonPath("$[0].startTime").value(activity1.getStartTime()))
                .andExpect(jsonPath("$[0].endTime").value(activity1.getEndTime()))
                .andExpect(jsonPath("$[0].roomName").value(activity1.getRoomName()))
                .andExpect(jsonPath("$[0].maxCapacity").value(activity1.getMaxCapacity()))
                .andExpect(jsonPath("$[1].idActivity").value(activity2.getIdActivity()))
                .andExpect(jsonPath("$[1].activityName").value(activity2.getActivityName()))
                .andExpect(jsonPath("$[1].description").value(activity2.getDescription()))
                .andExpect(jsonPath("$[1].activityType").value(activity2.getActivityType()))
                .andExpect(jsonPath("$[1].startTime").value(activity2.getStartTime()))
                .andExpect(jsonPath("$[1].endTime").value(activity2.getEndTime()))
                .andExpect(jsonPath("$[1].roomName").value(activity2.getRoomName()))
                .andExpect(jsonPath("$[1].maxCapacity").value(activity2.getMaxCapacity()));

        verify(activityService).getActivitiesByCongressId(congressId);
    }

    //--------------------------HELPER METHODS------------------


    private CreateActivityRequest createValidCreateActivityRequest(Long roomId, Integer activityTypeId) {
        return new CreateActivityRequest(
                roomId,
                activityTypeId,
                "Activity Name",
                "Activity Description",
                LocalDate.now().plusDays(1).atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay().plusHours(2),
                50
        );
    }

    private UpdateActivityRequest createValidUpdateActivityRequest(Integer maxCapacity) {
        return new UpdateActivityRequest(
                1L,
                "Updated Activity Name",
                "Updated Activity Description",
                LocalDate.now().plusDays(2).atStartOfDay(),
                LocalDate.now().plusDays(2).atStartOfDay().plusHours(2),
                maxCapacity
        );
    }

    private ActivityResponse createExpectedActivityResponse(Long idActivity, String roomName) {
        return new ActivityResponse(
                idActivity,
                "Activity Name",
                "Activity Description",
                "TALLER",
                LocalDate.now().plusDays(1).atStartOfDay().toString(),
                LocalDate.now().plusDays(1).atStartOfDay().plusHours(2).toString(),
                roomName,
                50
        );
    }

    private ActivityEntity createActivityEntity(Long idActivity, CongressEntity congress, RoomEntity room, Integer activityTypeId) {
        ActivityEntity activity = new ActivityEntity();
        activity.setIdActivity(idActivity);
        activity.setActivityName("Activity Name");
        activity.setDescription("Activity Description");
        activity.setStartTime(LocalDate.now().plusDays(1).atStartOfDay());
        activity.setEndTime(LocalDate.now().plusDays(1).atStartOfDay().plusHours(2));
        activity.setMaxCapacity(50);

        // Configura el tipo de actividad según el ID
        if (activityTypeId == 1) {
            activity.setActivityType(createActivityTypeEntity(1, "TALLER"));
        } else {
            activity.setActivityType(createActivityTypeEntity(activityTypeId, "PONENCIA"));
        }

        activity.setCongress(congress);
        activity.setRoom(room);

        return activity;
    }

    private ActivityTypeEntity createActivityTypeEntity(Integer id, String typeName) {
        ActivityTypeEntity activityType = new ActivityTypeEntity();
        activityType.setIdActivityType(id);
        activityType.setTypeName(typeName);
        return activityType;
    }

    private RoomEntity createRoom(Long id, String name, CongressEntity congress) {
        RoomEntity room = new RoomEntity();
        room.setIdRoom(id);
        room.setRoomName(name);
        room.setRoomCode("CODE" + id);
        room.setCapacity(100);
        room.setLocation("First Floor");
        room.setDescription("Description for " + name);
        room.setCongress(congress);
        return room;
    }


    private CongressEntity createCongress(Long id, String name, boolean isActive) {
        CongressEntity congress = new CongressEntity();
        congress.setIdCongress(id);
        congress.setCongressName(name);
        congress.setDescription("Description for " + name);
        congress.setStartDate(LocalDate.of(2026, 5, 15));
        congress.setEndDate(LocalDate.of(2026, 5, 17));
        congress.setLocation("Guatemala City");
        congress.setPrice(new BigDecimal("150.00"));
        congress.setIsActive(isActive);

        InstitutionEntity institution = new InstitutionEntity();
        institution.setIdInstitution(1L);
        institution.setInstitutionName("USAC");
        institution.setIsActive(true);
        congress.setInstitution(institution);

        return congress;
    }
}
