package com.alessandro.congress_management.repositories.room;

import com.alessandro.congress_management.models.rooms_and_activities.RoomEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    boolean existsByRoomNameAndCongress_IdCongress( String roomName, Long idCongress);

    boolean existsByRoomCodeAndCongress_IdCongress(String roomCode, Long idCongress);

    boolean existsByRoomNameAndCongress_IdCongressAndIdRoomNot(String roomName, Long idCongress, Long idRoom);

    boolean existsByRoomCodeAndCongress_IdCongressAndIdRoomNot(String roomCode, Long idCongress, Long idRoom);

    List<RoomEntity> getRoomsByCongress_IdCongress(Long idCongress);
}
