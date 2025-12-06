package com.example.ProjectTeam121.Repository;

import com.example.ProjectTeam121.Entity.ExportFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportFilterRepository extends JpaRepository<ExportFilter, Long> {
    // Tìm các bộ lọc của người dùng dựa trên email (định danh mới)
    List<ExportFilter> findByUser_Email(String email);
}