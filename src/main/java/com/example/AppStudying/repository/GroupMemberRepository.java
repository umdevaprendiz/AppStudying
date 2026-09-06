package com.example.AppStudying.repository;

import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupMember> findByGroupIdAndStatus(Long groupId, RequestStatus status);

    List<GroupMember> findByUserIdAndStatus(Long userId, RequestStatus status);

    long countByGroupIdAndStatus(Long groupId, RequestStatus status);

    @Modifying
    @Query("DELETE FROM GroupMember m WHERE m.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Query("DELETE FROM GroupMember m WHERE m.group.owner.id = :userId")
    void deleteByGroupOwnerId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM GroupMember m WHERE m.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
