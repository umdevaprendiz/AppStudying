package com.example.AppStudying.repository;

import com.example.AppStudying.model.Matter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatterRepository extends JpaRepository<Matter, Long> {
    boolean existsByNomeAndUserId(String nome, Long userId);
    List<Matter> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Matter m WHERE m.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}
