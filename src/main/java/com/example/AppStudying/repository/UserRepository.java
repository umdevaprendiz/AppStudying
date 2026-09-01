package com.example.AppStudying.repository;

import com.example.AppStudying.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByDeletionToken(String deletionToken);

    @Query("SELECT u.id FROM User u WHERE COALESCE(u.lastLoginAt, u.createdAt) < :limite")
    List<Long> findIdsInativosDesde(@Param("limite") LocalDateTime limite);

}
