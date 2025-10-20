package com.example.DemoProjectTinasoft.Repository;

import com.example.DemoProjectTinasoft.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}