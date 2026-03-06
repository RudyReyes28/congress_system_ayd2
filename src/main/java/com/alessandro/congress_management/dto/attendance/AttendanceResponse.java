package com.alessandro.congress_management.dto.attendance;

import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import lombok.Value;

@Value
public class AttendanceResponse {
    Long idAttendance;
    Long idActivity;
    String nameActivity;
    String nameUser;
    String emailUser;
    String participationType;

    public static AttendanceResponse fromEntity(AttendanceEntity attendanceEntity) {
        return new AttendanceResponse(
                attendanceEntity.getIdAttendance(),
                attendanceEntity.getActivity().getIdActivity(),
                attendanceEntity.getActivity().getActivityName(),
                attendanceEntity.getUser().getFullName(),
                attendanceEntity.getUser().getEmail(),
                attendanceEntity.getParticipationType().getTypeName()
        );
    }
}
