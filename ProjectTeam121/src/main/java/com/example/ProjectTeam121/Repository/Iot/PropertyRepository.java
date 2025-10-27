package com.example.ProjectTeam121.Repository.Iot;

import com.example.ProjectTeam121.Entity.Iot.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {
    boolean existsByName(String name);
}