package com.example.ProjectTeam121.Repository.Iot;

import com.example.ProjectTeam121.Entity.Iot.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, String> {
    Page<Sensor> findByDevice_Id(String deviceId, Pageable pageable);
}