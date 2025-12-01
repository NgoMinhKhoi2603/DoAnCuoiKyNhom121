package com.example.ProjectTeam121.Repository;

import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Entity.HistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, String > {

    /**
     * Find history entities by history type with pagination
     * @param historyType The type of history to filter by
     * @param pageable Pagination information
     * @return Page of HistoryEntity
     */
    Page<HistoryEntity> findByHistoryType(HistoryType historyType, Pageable pageable);

    Page<HistoryEntity> findByHistoryTypeAndIdentify(HistoryType historyType, String identify, Pageable pageable);

    void deleteByCreatedBy(String createdBy);
}
