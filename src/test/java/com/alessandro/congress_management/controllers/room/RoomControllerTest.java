package com.alessandro.congress_management.controllers.room;


import com.alessandro.congress_management.dto.room.CreateRoomRequest;
import com.alessandro.congress_management.dto.room.RoomResponse;
import com.alessandro.congress_management.dto.room.UpdateRoomRequest;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.institutions_and_system.InstitutionEntity;
import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.room.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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



@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    //-------------------------TESTS FOR GET ROOMS BY CONGRESS ID------------------

    @Test
    @WithMockUser(roles = "PARTICIPANT")
    public void testGetRoomsByCongressId_Success() throws Exception {
        // Arrange
        CongressEntity congress = createCongress(1L, "Congress 1", true);
        RoomEntity room1 = createRoom(1L, "Room 1", congress);
        RoomEntity room2 = createRoom(2L, "Room 2", congress);
        List<RoomEntity> rooms = Arrays.asList(room1, room2);

        when(roomService.getRoomsByCongressId(1L)).thenReturn(Arrays.asList(
                RoomResponse.fromEntity(room1),
                RoomResponse.fromEntity(room2)
        ));

        // Act & Assert
        mockMvc.perform(get("/api/v1/congresses/1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomName").value("Room 1"))
                .andExpect(jsonPath("$[1].roomName").value("Room 2"));

        verify(roomService).getRoomsByCongressId(1L);
    }

    //-------------------------TESTS FOR CREATE ROOM------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testCreateRoom_Success() throws Exception {
        // Arrange
        CongressEntity congress = createCongress(1L, "Congress 1", true);
        CreateRoomRequest request = createCreateRoomRequest("Room 1");
        RoomEntity createdRoom = createRoom(1L, "Room 1", congress);

        when(roomService.createRoom(any(CreateRoomRequest.class), eq(1L))).thenReturn(RoomResponse.fromEntity(createdRoom));

        // Act & Assert
        mockMvc.perform(post("/api/v1/congresses/1/rooms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomName").value("Room 1"))
                .andExpect(jsonPath("$.roomCode").value("CODE1"))
                .andExpect(jsonPath("$.capacity").value(100))
                .andExpect(jsonPath("$.location").value("First Floor"))
                .andExpect(jsonPath("$.description").value("Description for Room 1"));

        verify(roomService).createRoom(any(CreateRoomRequest.class), eq(1L));
    }

    //-------------------------TESTS FOR UPDATE ROOM------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testUpdateRoom_Success() throws Exception {
        // Arrange

        CongressEntity congress = createCongress(1L, "Congress 1", true);
        UpdateRoomRequest request = createUpdateRoomRequest("Updated Room");
        RoomEntity updatedRoom = createRoom(1L, "Updated Room", congress);

        when(roomService.updateRoom(eq(1L), any(UpdateRoomRequest.class))).thenReturn(RoomResponse.fromEntity(updatedRoom));

        // Act & Assert
        mockMvc.perform(put("/api/v1/congresses/rooms/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomName").value("Updated Room"))
                .andExpect(jsonPath("$.roomCode").value("CODE1"))
                .andExpect(jsonPath("$.capacity").value(100))
                .andExpect(jsonPath("$.location").value("First Floor"))
                .andExpect(jsonPath("$.description").value("Description for Updated Room"));

        verify(roomService).updateRoom(eq(1L), any(UpdateRoomRequest.class));
    }

    //-------------------------TESTS FOR DELETE ROOM------------------
    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testDeleteRoom_Success() throws Exception {
        // Arrange
        doNothing().when(roomService).deleteRoom(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/congresses/rooms/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(roomService).deleteRoom(1L);
    }



    // ---------------- HELPER METHODS -------------------

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

    private CreateRoomRequest createCreateRoomRequest(String name) {
        return new CreateRoomRequest(
                name,
                "CODE" + name.toUpperCase(),
                100,
                "First Floor",
                "Description for " + name
        );
    }

    private UpdateRoomRequest createUpdateRoomRequest(String name) {
        return new UpdateRoomRequest(
                name,
                "CODE" + name.toUpperCase(),
                150,
                "Second Floor",
                "Updated description for " + name
        );
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
