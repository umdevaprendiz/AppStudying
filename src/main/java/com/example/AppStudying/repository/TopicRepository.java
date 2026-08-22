package com.example.AppStudying.repository;

import com.example.AppStudying.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, String> {
}
