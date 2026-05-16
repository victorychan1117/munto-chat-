package com.example.muntochat.repository;

import com.example.muntochat.entity.Note;
import com.example.muntochat.entity.Participant;
import com.example.muntochat.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByReceiver(Participant receiver);
    List<Note> findByRoom(Room room);
    int countByReceiver(Participant receiver);
    boolean existsBySender(Participant sender);
    void deleteByRoom(Room room);
}
