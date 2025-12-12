package com.example.ProjectTeam121.Repository;

import com.example.ProjectTeam121.Entity.AISession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AISessionRepository extends JpaRepository<AISession, Long> {

    List<AISession> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    void deleteBySessionId(String sessionId);
}
