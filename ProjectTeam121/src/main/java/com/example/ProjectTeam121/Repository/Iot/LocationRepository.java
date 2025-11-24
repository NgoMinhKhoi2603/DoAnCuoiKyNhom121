//package com.example.ProjectTeam121.Repository.Iot;
//
//import com.example.ProjectTeam121.Entity.Iot.Location;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface LocationRepository extends JpaRepository<Location, String> {
//    // Tìm các vị trí (cấp cao nhất) của toàn công ty
//    Page<Location> findByParentIsNull(Pageable pageable);
//
//    // Tìm các vị trí con của một vị trí cha
//    Page<Location> findByParent_Id(String parentId, Pageable pageable);
//}