package com.example.AppStudying.repository;

import com.example.AppStudying.model.TimeLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimeLineRepository extends JpaRepository<TimeLine, Long> {
    List<TimeLine> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM TimeLine t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
