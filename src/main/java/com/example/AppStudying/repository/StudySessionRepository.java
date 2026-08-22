package com.example.AppStudying.repository;

import com.example.AppStudying.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, String> {
}
