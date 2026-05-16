package com.example.muntochat.entity;

import com.example.muntochat.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Builder.Default
    private int noteReceivedCount = 0;
}
