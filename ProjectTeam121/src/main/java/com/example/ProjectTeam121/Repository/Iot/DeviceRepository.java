package com.example.ProjectTeam121.Repository.Iot;

import com.example.ProjectTeam121.Entity.Iot.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    boolean existsByUniqueIdentifier(String uniqueIdentifier);
//    Page<Device> findByLocation_IdAndUser_Username(String locationId, String username, Pageable pageable);
}