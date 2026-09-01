package com.example.AppStudying.repository;

import com.example.AppStudying.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByMatterId(Long matterId);

    @Modifying
    @Query("DELETE FROM Topic t WHERE t.matter.user.id = :userId")
    void deleteByMatterUserId(@Param("userId") Long userId);
}
