package com.example.AppStudying.repository;

import com.example.AppStudying.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Modifying
    @Query("DELETE FROM StudyGroup g WHERE g.owner.id = :userId")
    void deleteByOwnerId(@Param("userId") Long userId);
}
