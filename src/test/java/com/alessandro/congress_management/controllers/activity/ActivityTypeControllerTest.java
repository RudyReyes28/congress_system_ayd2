package com.alessandro.congress_management.controllers.activity;
import com.alessandro.congress_management.dto.activity.ActivityTypeResponse;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.activity.ActivityTypeService;
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

@WebMvcTest(ActivityTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ActivityTypeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityTypeService activityTypeService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    //-------------------------TESTS FOR GET ALL ACTIVITY TYPES------------------

    @Test
    @WithMockUser(roles = "ADMIN_CONGRESS")
    public void testGetAllActivityTypes() throws Exception {
        List<ActivityTypeResponse> activityTypes = Arrays.asList(
                new ActivityTypeResponse(1, "TALLER"),
                new ActivityTypeResponse(2, "PONENCIA")
        );

        when(activityTypeService.getAllActivityTypes()).thenReturn(activityTypes);

        mockMvc.perform(get("/api/v1/activity-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(activityTypes.size()))
                .andExpect(jsonPath("$[0].idActivityType").value(activityTypes.get(0).getIdActivityType()))
                .andExpect(jsonPath("$[0].activityTypeName").value(activityTypes.get(0).getActivityTypeName()))
                .andExpect(jsonPath("$[1].idActivityType").value(activityTypes.get(1).getIdActivityType()))
                .andExpect(jsonPath("$[1].activityTypeName").value(activityTypes.get(1).getActivityTypeName()));

        verify(activityTypeService, times(1)).getAllActivityTypes();
    }
}
