package com.example.muntochat.repository;

import com.example.muntochat.entity.Participant;
import com.example.muntochat.entity.Room;
import com.example.muntochat.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByRoom(Room room);
    Optional<Participant> findByRoomAndNickname(Room room, String nickname);
    List<Participant> findByRoomAndGenderNot(Room room, Gender gender);
    List<Participant> findByRoomOrderByNoteReceivedCountDesc(Room room);
    void deleteByRoom(Room room);
}
