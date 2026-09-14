package com.autocare.vehicleservice.repository;

import com.autocare.vehicleservice.entity.Branch;
import com.autocare.vehicleservice.enums.BranchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByStatus(BranchStatus status);
}