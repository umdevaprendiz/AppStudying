package com.example.AppStudying.repository;

import com.example.AppStudying.model.StudyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyRequestRepository extends JpaRepository<StudyRequest, Long> {
    List<StudyRequest> findByReceiverId(Long receiverId);
    List<StudyRequest> findByRequesterId(Long requesterId);

    @Modifying
    @Query("DELETE FROM StudyRequest r WHERE r.requester.id = :userId OR r.receiver.id = :userId OR r.matter.user.id = :userId")
    void deleteEnvolvendoUsuario(@Param("userId") Long userId);
}
