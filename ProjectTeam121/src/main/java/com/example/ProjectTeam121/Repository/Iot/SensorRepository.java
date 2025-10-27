package com.example.ProjectTeam121.Repository.Iot;

import com.example.ProjectTeam121.Entity.Iot.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, String> {

    // Tìm sensor theo ID và username của chủ sở hữu thiết bị
    @Query("SELECT s FROM Sensor s WHERE s.id = :id AND s.device.user.username = :username")
    Optional<Sensor> findByIdAndDeviceUserUsername(String id, String username);

    // Tìm sensor theo deviceId và username của chủ sở hữu thiết bị
    @Query("SELECT s FROM Sensor s WHERE s.device.id = :deviceId AND s.device.user.username = :username")
    Page<Sensor> findByDevice_IdAndDeviceUserUsername(String deviceId, String username, Pageable pageable);
}