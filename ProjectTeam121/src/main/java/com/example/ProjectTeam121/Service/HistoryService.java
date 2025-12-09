package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Response.HistoryResponse;
import com.example.ProjectTeam121.Entity.BaseEntity;
import com.example.ProjectTeam121.Entity.HistoryEntity;
import com.example.ProjectTeam121.Mapper.HistoryMapper;
import com.example.ProjectTeam121.Repository.HistoryRepository;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final ObjectMapper objectMapper;
    private final HistoryMapper historyMapper;

    /* ------------------------------------------------------------------
     * 1. SAVE HISTORY (SINGLE)
     * ------------------------------------------------------------------ */
    @Async
    public <T> void saveHistory(T entity, ActionLog action, HistoryType historyType, String identify, String createdBy, String description) {
        try {
            String jsonContent = convertEntityToJson(entity);

            HistoryEntity history = HistoryEntity.builder()
                    .action(action)
                    .historyType(historyType)
                    .content(jsonContent)
                    .createdBy(createdBy)
                    .identify(identify)
                    .description(description)
                    .build();

            historyRepository.save(history);

            log.info("Saved history for entity: {} with action: {} and type: {}",
                    entity.getClass().getSimpleName(), action, historyType);

        } catch (Exception e) {
            log.warn("Error saving entity history for: {} action: {} type: {}",
                    entity.getClass().getSimpleName(), action, historyType, e);
        }
    }

    /* ------------------------------------------------------------------
     * 2. SAVE HISTORY (BATCH)
     * ------------------------------------------------------------------ */
    @Async
    public <T> void saveBatchHistory(List<T> entities, ActionLog action, HistoryType historyType,
                                    List<String> identifiers, String createdBy) {

        try {
            if (entities == null || entities.isEmpty()) {
                log.warn("Batch history save skipped: empty entity list");
                return;
            }

            List<HistoryEntity> historyEntities = new ArrayList<>();

            for (int i = 0; i < entities.size(); i++) {
                T entity = entities.get(i);
                String identifier = identifiers == null ? null : identifiers.get(i);

                HistoryEntity historyEntity =
                        createHistoryEntity(entity, action, historyType, identifier, createdBy);

                if (historyEntity != null) {
                    historyEntities.add(historyEntity);
                }
            }

            if (!historyEntities.isEmpty()) {
                historyRepository.saveAll(historyEntities);

                log.info("Saved batch history: {} items, action: {}, type: {}",
                        historyEntities.size(), action, historyType);
            }

        } catch (Exception e) {
            log.warn("Error saving batch history - action: {} type: {}", action, historyType, e);
        }
    }

    /* ------------------------------------------------------------------
     * 3. GET ALL HISTORY (MAIN API FOR FRONTEND)
     * ------------------------------------------------------------------ */
    public Page<HistoryResponse> getAllHistory(Pageable pageable) {
        try {
            Page<HistoryEntity> entities =
                    historyRepository.findAllByOrderByCreateDateDesc(pageable);

            return historyMapper.toResponsePage(entities);

        } catch (Exception e) {
            log.error("Error retrieving all history records", e);
            throw new RuntimeException("Failed to load history");
        }
    }

    /* ------------------------------------------------------------------
     * 4. GET HISTORY BY TYPE
     * ------------------------------------------------------------------ */
    public Page<HistoryResponse> getHistoryByType(HistoryType historyType, Pageable pageable) {
        try {
            Page<HistoryEntity> historyEntities =
                    historyRepository.findByHistoryType(historyType, pageable);

            return historyMapper.toResponsePage(historyEntities);

        } catch (Exception e) {
            log.error("Error retrieving history by type: {}", historyType, e);
            throw new RuntimeException("Failed to retrieve history by type");
        }
    }

    /* ------------------------------------------------------------------
     * 5. GET HISTORY BY TYPE + IDENTIFY
     * ------------------------------------------------------------------ */
    public Page<HistoryResponse> getHistoryByTypeAndIdentify(
            HistoryType historyType, String identify, Pageable pageable) {

        return historyMapper.toResponsePage(
                historyRepository.findByHistoryTypeAndIdentify(historyType, identify, pageable)
        );
    }

    /* ------------------------------------------------------------------
     * 6. CONVERT ENTITY TO JSON (FILTER BASEENTITY FIELDS)
     * ------------------------------------------------------------------ */
    private <T> String convertEntityToJson(T entity) throws Exception {
        if (entity instanceof BaseEntity) {

            ObjectMapper filteredMapper = objectMapper.copy();

            SimpleBeanPropertyFilter baseEntityFilter =
                    SimpleBeanPropertyFilter.serializeAllExcept(
                            "id", "flagStatus", "isDeleted", "version",
                            "createdBy", "createDate", "lastUpdatedBy", "lastUpdateDate"
                    );

            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter("baseEntityFilter", baseEntityFilter)
                    .setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());

            filteredMapper.setFilterProvider(filters);
            filteredMapper.addMixIn(BaseEntity.class, BaseEntityFilterMixIn.class);

            return filteredMapper.writeValueAsString(entity);
        }

        return objectMapper.writeValueAsString(entity);
    }

    @JsonFilter("baseEntityFilter")
    private interface BaseEntityFilterMixIn {}

    /* ------------------------------------------------------------------
     * 7. INTERNAL UTIL – Build HistoryEntity safely
     * ------------------------------------------------------------------ */
    private <T> HistoryEntity createHistoryEntity(T entity, ActionLog action, HistoryType historyType,
                                                  String identify, String createdBy) {
        try {
            String jsonContent = convertEntityToJson(entity);

            return HistoryEntity.builder()
                    .action(action)
                    .historyType(historyType)
                    .content(jsonContent)
                    .createdBy(createdBy)
                    .identify(identify)
                    .build();

        } catch (Exception e) {
            log.warn("JSON conversion failed for entity: {}", entity.getClass().getSimpleName(), e);
            return null;
        }
    }
}
