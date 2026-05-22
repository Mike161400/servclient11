package com.rentalservice.antivirus.auth.repository;

import com.rentalservice.antivirus.auth.entity.Role;
import com.rentalservice.antivirus.auth.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);
}
