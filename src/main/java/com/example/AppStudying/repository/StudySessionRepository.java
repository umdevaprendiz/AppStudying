package com.example.AppStudying.repository;

import com.example.AppStudying.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByUserId(Long userId);
    Optional<StudySession> findByUserIdAndMatterIdAndFimIsNull(Long userId, Long matterId);

    @Modifying
    @Query("DELETE FROM StudySession s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
