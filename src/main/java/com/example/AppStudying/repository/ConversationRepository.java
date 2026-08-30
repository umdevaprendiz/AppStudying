package com.example.AppStudying.repository;

import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE (c.requester.id = :userA AND c.receiver.id = :userB) " +
            "OR (c.requester.id = :userB AND c.receiver.id = :userA)")
    Optional<Conversation> findEntreUsuarios(@Param("userA") Long userA, @Param("userB") Long userB);

    List<Conversation> findByReceiverIdAndStatus(Long receiverId, RequestStatus status);

    @Query("SELECT c FROM Conversation c WHERE (c.requester.id = :userId OR c.receiver.id = :userId) AND c.status = :status")
    List<Conversation> findPorUsuarioEStatus(@Param("userId") Long userId, @Param("status") RequestStatus status);

    long countByReceiverIdAndStatus(Long receiverId, RequestStatus status);
}
