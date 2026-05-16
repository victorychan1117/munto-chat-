package com.example.muntochat.service;

import com.example.muntochat.entity.Note;
import com.example.muntochat.entity.Participant;
import com.example.muntochat.entity.Room;
import com.example.muntochat.enums.Gender;
import com.example.muntochat.repository.NoteRepository;
import com.example.muntochat.repository.ParticipantRepository;
import com.example.muntochat.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final ParticipantRepository participantRepository;
    private final RoomRepository roomRepository;

    public boolean hasSentNote(String roomCode, String senderNickname) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        Participant sender = participantRepository.findByRoomAndNickname(room, senderNickname)
                .orElse(null);
        if (sender == null) return false;
        return noteRepository.existsBySender(sender);
    }

    public Note sendNote(String roomCode, String senderNickname, Long receiverId, String content) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        Participant sender = participantRepository.findByRoomAndNickname(room, senderNickname)
                .orElseThrow(() -> new IllegalArgumentException("보내는 사람을 찾을 수 없습니다."));

        if (noteRepository.existsBySender(sender)) {
            throw new IllegalStateException("이미 쪽지를 보냈습니다.");
        }

        Participant receiver = participantRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없습니다."));

        receiver.setNoteReceivedCount(receiver.getNoteReceivedCount() + 1);
        participantRepository.save(receiver);

        Note note = Note.builder()
                .room(room)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();
        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotesForRoom(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        return noteRepository.findByRoom(room);
    }

    @Transactional(readOnly = true)
    public List<Note> getNotesForParticipant(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다."));
        return noteRepository.findByReceiver(participant);
    }

    @Transactional(readOnly = true)
    public List<Participant> getRanking(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        List<Participant> all = participantRepository.findByRoomOrderByNoteReceivedCountDesc(room);
        return all.stream().limit(3).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Participant> getRankingByGender(String roomCode, Gender gender) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        List<Participant> all = participantRepository.findByRoomOrderByNoteReceivedCountDesc(room);
        return all.stream()
                .filter(p -> p.getGender() == gender)
                .limit(3)
                .collect(Collectors.toList());
    }
}
