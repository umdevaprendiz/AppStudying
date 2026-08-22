package com.example.AppStudying.repository;

import com.example.AppStudying.model.Matter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatterRepository extends JpaRepository<Matter, Long> {
    boolean existsByNomeAndUserId(String nome, Long userId);
    List<Matter> findByUserId(Long userId);


}
