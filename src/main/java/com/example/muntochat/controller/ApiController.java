package com.example.muntochat.controller;

import com.example.muntochat.entity.Participant;
import com.example.muntochat.entity.Room;
import com.example.muntochat.enums.Gender;
import com.example.muntochat.enums.RoomMode;
import com.example.muntochat.service.NoteService;
import com.example.muntochat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final NoteService noteService;
    private final RoomService roomService;

    @GetMapping("/room/{roomCode}/ranking")
    public ResponseEntity<?> getRanking(@PathVariable String roomCode) {
        Room room;
        try {
            room = roomService.getRoom(roomCode);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("roomName", room.getName());
        result.put("mode", room.getMode().name());

        List<Participant> overall = noteService.getRanking(roomCode);
        result.put("overall", toRankingList(overall));

        if (room.getMode() == RoomMode.GENDER) {
            List<Participant> maleRanking = noteService.getRankingByGender(roomCode, Gender.MALE);
            List<Participant> femaleRanking = noteService.getRankingByGender(roomCode, Gender.FEMALE);
            result.put("male", toRankingList(maleRanking));
            result.put("female", toRankingList(femaleRanking));
        }

        return ResponseEntity.ok(result);
    }

    private List<Map<String, Object>> toRankingList(List<Participant> participants) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            Participant p = participants.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i + 1);
            item.put("nickname", p.getNickname());
            item.put("gender", p.getGender() != null ? p.getGender().name() : null);
            item.put("count", p.getNoteReceivedCount());
            list.add(item);
        }
        return list;
    }
}
