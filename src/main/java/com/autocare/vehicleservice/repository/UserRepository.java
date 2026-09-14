package com.autocare.vehicleservice.repository;

import com.autocare.vehicleservice.entity.User;
import com.autocare.vehicleservice.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByBranch_IdAndRole(Long branchId, Role role);

    List<User> findByBranch_IdAndRoleNot(Long branchId, Role role);
}
