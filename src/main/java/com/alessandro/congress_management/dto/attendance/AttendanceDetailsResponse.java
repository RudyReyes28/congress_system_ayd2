package com.alessandro.congress_management.dto.attendance;

import com.alessandro.congress_management.models.attendance.AttendanceEntity;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class AttendanceDetailsResponse {
    Long idAttendance;
    String nameActivity;
    String nameCongress;
    String descriptionActivity;
    String nameUser;
    String identificationUser;
    String emailUser;
    String participationType;
    String nameUserRecording;
    LocalDateTime attendanceDate;

    public static AttendanceDetailsResponse fromEntity(AttendanceEntity attendanceEntity) {
        return new AttendanceDetailsResponse(
                attendanceEntity.getIdAttendance(),
                attendanceEntity.getActivity().getActivityName(),
                attendanceEntity.getActivity().getCongress().getCongressName(),
                attendanceEntity.getActivity().getDescription(),
                attendanceEntity.getUser().getFullName(),
                attendanceEntity.getUser().getIdentificationNumber(),
                attendanceEntity.getUser().getEmail(),
                attendanceEntity.getParticipationType().getTypeName(),
                attendanceEntity.getRecordedBy().getFullName(),
                attendanceEntity.getRecordedAt()
        );
    }
}
