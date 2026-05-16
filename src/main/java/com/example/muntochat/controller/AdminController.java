package com.example.muntochat.controller;

import com.example.muntochat.entity.Note;
import com.example.muntochat.entity.Participant;
import com.example.muntochat.entity.ParticipantPool;
import com.example.muntochat.entity.Room;
import com.example.muntochat.enums.Gender;
import com.example.muntochat.enums.RoomMode;
import com.example.muntochat.repository.ParticipantPoolRepository;
import com.example.muntochat.service.NoteService;
import com.example.muntochat.service.RoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String ADMIN_PASSWORD = "1004";
    private static final String ADMIN_AUTH_KEY = "adminAuth";

    private final RoomService roomService;
    private final NoteService noteService;
    private final ParticipantPoolRepository poolRepository;

    @GetMapping
    public String index(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        model.addAttribute("pool", poolRepository.findAll());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("lastGender", session.getAttribute("lastGender"));
        return "admin/index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String password, HttpSession session, Model model) {
        if (ADMIN_PASSWORD.equals(password)) {
            session.setAttribute(ADMIN_AUTH_KEY, true);
            return "redirect:/admin";
        }
        model.addAttribute("error", "비밀번호가 틀렸습니다.");
        return "admin/login";
    }

    // Pool management
    @PostMapping("/pool/add")
    public String addToPool(@RequestParam String nickname,
                            @RequestParam Gender gender,
                            HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        if (poolRepository.findByNickname(nickname).isPresent()) {
            model.addAttribute("pool", poolRepository.findAll());
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("lastGender", gender);
            model.addAttribute("error", "이미 등록된 별칭입니다: " + nickname);
            return "admin/index";
        }
        poolRepository.save(ParticipantPool.builder().nickname(nickname).gender(gender).build());
        session.setAttribute("lastGender", gender);
        return "redirect:/admin";
    }

    @PostMapping("/pool/{id}/delete")
    public String removeFromPool(@PathVariable Long id, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        poolRepository.deleteById(id);
        return "redirect:/admin";
    }

    // Room creation
    @PostMapping("/create")
    public String createRoom(@RequestParam String name,
                             @RequestParam RoomMode mode,
                             HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        List<ParticipantPool> pool = poolRepository.findAll();
        Room room = roomService.createRoom(name, mode);
        for (ParticipantPool p : pool) {
            roomService.addParticipant(room.getRoomCode(), p.getNickname(), p.getGender());
        }
        return "redirect:/admin/room/" + room.getRoomCode();
    }

    @PostMapping("/room/{roomCode}/delete")
    public String deleteRoom(@PathVariable String roomCode, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        roomService.deleteRoom(roomCode);
        return "redirect:/admin";
    }

    @GetMapping("/room/{roomCode}")
    public String roomDashboard(@PathVariable String roomCode, Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        Room room = roomService.getRoom(roomCode);
        List<Participant> participants = roomService.getParticipants(roomCode);
        model.addAttribute("room", room);
        model.addAttribute("participants", participants);
        return "admin/room";
    }

    @PostMapping("/room/{roomCode}/participant")
    public String addParticipant(@PathVariable String roomCode,
                                 @RequestParam String nickname,
                                 @RequestParam(required = false) Gender gender,
                                 HttpSession session,
                                 Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        try {
            roomService.addParticipant(roomCode, nickname, gender);
        } catch (IllegalArgumentException e) {
            Room room = roomService.getRoom(roomCode);
            List<Participant> participants = roomService.getParticipants(roomCode);
            model.addAttribute("room", room);
            model.addAttribute("participants", participants);
            model.addAttribute("error", e.getMessage());
            return "admin/room";
        }
        return "redirect:/admin/room/" + roomCode;
    }

    @PostMapping("/room/{roomCode}/participant/{id}/delete")
    public String removeParticipant(@PathVariable String roomCode, @PathVariable Long id, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        roomService.removeParticipant(id);
        return "redirect:/admin/room/" + roomCode;
    }

    @GetMapping("/room/{roomCode}/notes-all")
    public String viewAllNotes(@PathVariable String roomCode, Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        Room room = roomService.getRoom(roomCode);
        List<Note> notes = noteService.getAllNotesForRoom(roomCode);
        model.addAttribute("room", room);
        model.addAttribute("notes", notes);
        return "admin/notes-all";
    }

    @GetMapping("/room/{roomCode}/notes/{participantId}")
    public String viewNotes(@PathVariable String roomCode,
                            @PathVariable Long participantId,
                            Model model,
                            HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            return "redirect:/admin/login";
        }
        Room room = roomService.getRoom(roomCode);
        List<Note> notes = noteService.getNotesForParticipant(participantId);
        Participant participant = notes.isEmpty() ? null : notes.get(0).getReceiver();

        if (participant == null) {
            List<Participant> participants = roomService.getParticipants(roomCode);
            participant = participants.stream()
                    .filter(p -> p.getId().equals(participantId))
                    .findFirst().orElse(null);
        }

        model.addAttribute("room", room);
        model.addAttribute("participant", participant);
        model.addAttribute("notes", notes);
        return "admin/notes";
    }
}
