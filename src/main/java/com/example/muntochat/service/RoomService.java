package com.example.muntochat.service;

import com.example.muntochat.entity.Participant;
import com.example.muntochat.entity.Room;
import com.example.muntochat.enums.Gender;
import com.example.muntochat.enums.RoomMode;
import com.example.muntochat.repository.NoteRepository;
import com.example.muntochat.repository.ParticipantRepository;
import com.example.muntochat.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final NoteRepository noteRepository;

    public Room createRoom(String name, RoomMode mode) {
        Room room = Room.builder()
                .name(name)
                .mode(mode)
                .roomCode(generateRoomCode())
                .build();
        return roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Room getRoom(String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + roomCode));
    }

    public Participant addParticipant(String roomCode, String nickname, Gender gender) {
        Room room = getRoom(roomCode);

        if (participantRepository.findByRoomAndNickname(room, nickname).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 별칭입니다: " + nickname);
        }

        Participant participant = Participant.builder()
                .room(room)
                .nickname(nickname)
                .gender(gender)
                .noteReceivedCount(0)
                .build();
        return participantRepository.save(participant);
    }

    public void removeParticipant(Long participantId) {
        participantRepository.deleteById(participantId);
    }

    public void deleteRoom(String roomCode) {
        Room room = getRoom(roomCode);
        noteRepository.deleteByRoom(room);
        participantRepository.deleteByRoom(room);
        roomRepository.delete(room);
    }

    @Transactional(readOnly = true)
    public List<Participant> getParticipants(String roomCode) {
        Room room = getRoom(roomCode);
        return participantRepository.findByRoom(room);
    }

    private String generateRoomCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
