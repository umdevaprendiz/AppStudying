package com.example.AppStudying.repository;

import com.example.AppStudying.model.StudyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyRequestRepository extends JpaRepository<StudyRequest, Long> {
    List<StudyRequest> findByReceiverId(Long receiverId);
    List<StudyRequest> findByRequesterId(Long requesterId);
}
