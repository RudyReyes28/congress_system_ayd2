package com.alessandro.congress_management.services.activity;

import com.alessandro.congress_management.dto.activity.ActivityTypeResponse;
import com.alessandro.congress_management.models.rooms_and_activities.ActivityTypeEntity;
import com.alessandro.congress_management.repositories.activity.ActivityTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityTypesServiceImplTest {
    @Mock
    private ActivityTypeRepository activityTypeRepository;

    @InjectMocks
    private ActivityTypeServiceImpl activityTypesService;

    @Test
    public void testGetAllActivityTypes() {
        // Arrange
        ActivityTypeEntity type1 = new ActivityTypeEntity();
        type1.setIdActivityType(1);
        type1.setTypeName("TALLER");

        ActivityTypeEntity type2 = new ActivityTypeEntity();
        type2.setIdActivityType(2);
        type2.setTypeName("PONENCIA");

        when(activityTypeRepository.findAll()).thenReturn(List.of(type1, type2));

        // Act
        List<ActivityTypeResponse> response = activityTypesService.getAllActivityTypes();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("TALLER", response.get(0).getActivityTypeName());
        assertEquals("PONENCIA", response.get(1).getActivityTypeName());
    }
}
