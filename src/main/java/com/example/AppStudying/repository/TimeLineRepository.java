package com.example.AppStudying.repository;

import com.example.AppStudying.model.TimeLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeLineRepository extends JpaRepository<TimeLine, Long> {
    List<TimeLine> findByUserId(Long userId);
}
