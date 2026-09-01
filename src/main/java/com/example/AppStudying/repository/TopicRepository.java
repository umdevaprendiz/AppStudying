package com.example.AppStudying.repository;

import com.example.AppStudying.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByMatterId(Long matterId);
}
