package com.example.AppStudying.repository;

import com.example.AppStudying.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :userA AND m.receiver.id = :userB) " +
            "OR (m.sender.id = :userB AND m.receiver.id = :userA) ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversa(@Param("userA") Long userA, @Param("userB") Long userB);
}
