package com.alessandro.congress_management.services.reports;

import com.alessandro.congress_management.dto.reports.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.time.LocalDateTime;

public interface CongressAdminReportService {

    ParticipantsReportResponse getParticipantsReport(Long idCongress, Long idAdmin, String participationType)
            throws NotFoundException, BusinessRuleException;


    ActivityAttendanceReportResponse getActivityAttendanceReport(Long idCongress, Long idAdmin, Long idActivity, Long idRoom,
            LocalDateTime startDate, LocalDateTime endDate)
            throws NotFoundException, BusinessRuleException;


    WorkshopReservationReportResponse getWorkshopReservationReport(Long idCongress, Long idAdmin, Long idActivity)
            throws NotFoundException, BusinessRuleException;

    CongressEarningsReportResponse getCongressEarningsReport(Long idCongress, Long idAdmin)
            throws NotFoundException, BusinessRuleException;
}
