package com.example.ProjectTeam121.Repository.Iot;

import com.example.ProjectTeam121.Entity.Iot.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {
    // Tìm các vị trí (cấp cao nhất) của một user
    Page<Location> findByUser_UsernameAndParentIsNull(String username, Pageable pageable);

    // Tìm các vị trí con của một vị trí cha
    Page<Location> findByParent_Id(String parentId, Pageable pageable);

    Optional<Location> findByIdAndUser_Username(String id, String username);
}