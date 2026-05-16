package com.example.muntochat.repository;

import com.example.muntochat.entity.ParticipantPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantPoolRepository extends JpaRepository<ParticipantPool, Long> {
    Optional<ParticipantPool> findByNickname(String nickname);
}
