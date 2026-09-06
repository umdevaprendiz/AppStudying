package com.example.AppStudying.repository;

import com.example.AppStudying.model.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    List<GroupMessage> findByGroupIdOrderBySentAtAsc(Long groupId);

    @Modifying
    @Query("DELETE FROM GroupMessage m WHERE m.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Query("DELETE FROM GroupMessage m WHERE m.group.owner.id = :userId")
    void deleteByGroupOwnerId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM GroupMessage m WHERE m.sender.id = :userId")
    void deleteBySenderId(@Param("userId") Long userId);
}
