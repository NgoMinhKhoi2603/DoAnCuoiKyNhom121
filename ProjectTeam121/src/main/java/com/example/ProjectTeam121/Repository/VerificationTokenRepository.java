package com.example.ProjectTeam121.Repository;

import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
    @Transactional
    void deleteByUser(User user);

    VerificationToken findByUserId(Long userId);
}