package com.example.ProjectTeam121.Repository.Iot;

import com.example.ProjectTeam121.Entity.Iot.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    boolean existsByUniqueIdentifier(String uniqueIdentifier);

    Page<Device> findAllByCreatedBy(String createdBy, Pageable pageable);

    Optional<Device> findByUniqueIdentifier(String uniqueId);

    @Query("SELECT d FROM Device d WHERE " +
            "(:province IS NULL OR d.province = :province) AND " +
            "(:district IS NULL OR d.district = :district) AND " +
            "(:ward IS NULL OR d.ward = :ward) AND " +
            "(:location IS NULL OR d.location LIKE %:location%) AND " +
            "(:deviceTypeId IS NULL OR d.deviceType.id = :deviceTypeId)") // <--- DÒNG NÀY ĐÃ SỬA
    List<Device> findByLocationAndType(
            @Param("province") String province,
            @Param("district") String district,
            @Param("ward") String ward,
            @Param("location") String location,
            @Param("deviceTypeId") String deviceTypeId // <--- Tham số mới
    );

    long countByCreatedBy(String createdBy);
}