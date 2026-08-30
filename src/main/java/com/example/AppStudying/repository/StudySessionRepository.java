package com.example.AppStudying.repository;

import com.example.AppStudying.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByUserId(Long userId);
    Optional<StudySession> findByUserIdAndMatterIdAndFimIsNull(Long userId, Long matterId);
}
