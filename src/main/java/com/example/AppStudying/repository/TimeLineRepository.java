package com.example.AppStudying.repository;

import com.example.AppStudying.model.TimeLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeLineRepository extends JpaRepository<TimeLine, String> {
}
