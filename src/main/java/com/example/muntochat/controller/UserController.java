package com.example.muntochat.controller;

import com.example.muntochat.entity.Participant;
import com.example.muntochat.entity.Room;
import com.example.muntochat.enums.RoomMode;
import com.example.muntochat.repository.ParticipantRepository;
import com.example.muntochat.service.NoteService;
import com.example.muntochat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/room")
@RequiredArgsConstructor
public class UserController {

    private final RoomService roomService;
    private final NoteService noteService;
    private final ParticipantRepository participantRepository;

    @GetMapping("/{roomCode}")
    public String enterPage(@PathVariable String roomCode, Model model) {
        Room room = roomService.getRoom(roomCode);
        model.addAttribute("room", room);
        return "user/enter";
    }

    @PostMapping("/{roomCode}/enter")
    public String enter(@PathVariable String roomCode, @RequestParam String nickname, Model model) {
        Room room = roomService.getRoom(roomCode);
        Participant participant = participantRepository.findByRoomAndNickname(room, nickname).orElse(null);

        if (participant == null) {
            model.addAttribute("room", room);
            model.addAttribute("error", "등록되지 않은 별칭입니다. 관리자에게 문의하세요.");
            return "user/enter";
        }

        if (noteService.hasSentNote(roomCode, nickname)) {
            model.addAttribute("room", room);
            model.addAttribute("error", "이미 쪽지를 보냈습니다. 한 번만 보낼 수 있어요.");
            return "user/enter";
        }

        return "redirect:/room/" + roomCode + "/select?nickname=" + URLEncoder.encode(nickname, StandardCharsets.UTF_8);
    }

    @GetMapping("/{roomCode}/select")
    public String selectPage(@PathVariable String roomCode, @RequestParam String nickname, Model model) {
        Room room = roomService.getRoom(roomCode);
        Participant me = participantRepository.findByRoomAndNickname(room, nickname)
                .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다."));

        List<Participant> candidates;
        if (room.getMode() == RoomMode.GENDER && me.getGender() != null) {
            candidates = participantRepository.findByRoomAndGenderNot(room, me.getGender());
        } else {
            candidates = participantRepository.findByRoom(room);
        }

        // Exclude self
        candidates = candidates.stream()
                .filter(p -> !p.getId().equals(me.getId()))
                .collect(Collectors.toList());

        model.addAttribute("room", room);
        model.addAttribute("nickname", nickname);
        model.addAttribute("candidates", candidates);
        return "user/select";
    }

    @GetMapping("/{roomCode}/write")
    public String writePage(@PathVariable String roomCode,
                            @RequestParam String nickname,
                            @RequestParam Long receiverId,
                            Model model) {
        Room room = roomService.getRoom(roomCode);
        Participant receiver = participantRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없습니다."));

        model.addAttribute("room", room);
        model.addAttribute("nickname", nickname);
        model.addAttribute("receiver", receiver);
        return "user/write";
    }

    @PostMapping("/{roomCode}/send")
    public String sendNote(@PathVariable String roomCode,
                           @RequestParam String nickname,
                           @RequestParam Long receiverId,
                           @RequestParam String content,
                           Model model) {
        if (noteService.hasSentNote(roomCode, nickname)) {
            Room room = roomService.getRoom(roomCode);
            model.addAttribute("room", room);
            model.addAttribute("error", "이미 쪽지를 보냈습니다. 한 번만 보낼 수 있어요.");
            return "user/enter";
        }
        noteService.sendNote(roomCode, nickname, receiverId, content);
        return "redirect:/room/" + roomCode + "/done?nickname=" + URLEncoder.encode(nickname, StandardCharsets.UTF_8);
    }

    @GetMapping("/{roomCode}/done")
    public String donePage(@PathVariable String roomCode,
                           @RequestParam String nickname,
                           Model model) {
        Room room = roomService.getRoom(roomCode);
        model.addAttribute("room", room);
        model.addAttribute("nickname", nickname);
        return "user/done";
    }

    @GetMapping("/{roomCode}/ranking")
    public String rankingPage(@PathVariable String roomCode,
                              @RequestParam(required = false) String from,
                              Model model) {
        Room room = roomService.getRoom(roomCode);
        model.addAttribute("room", room);
        model.addAttribute("from", from);
        return "ranking";
    }
}
