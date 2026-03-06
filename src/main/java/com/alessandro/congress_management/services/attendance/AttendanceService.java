package com.alessandro.congress_management.services.attendance;

import com.alessandro.congress_management.dto.attendance.AttendanceDetailsResponse;
import com.alessandro.congress_management.dto.attendance.AttendanceResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.util.List;

public interface AttendanceService {

    AttendanceResponse recordAttendance(Long idActivity, Long idAdminUser, Long idUser) throws NotFoundException, BusinessRuleException;

    List<AttendanceDetailsResponse> getMyAttendanceDetails(Long idUser);

    List<AttendanceDetailsResponse> getAttendanceDetailsByActivity(Long idActivity, Long idAdminUser) throws NotFoundException;

    List<AttendanceDetailsResponse> getAttendanceDetailsByCongress(Long idCongress, Long idAdminUser) throws NotFoundException;
}
