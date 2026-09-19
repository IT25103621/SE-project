package com.autocare.vehicleservice.repository;

import com.autocare.vehicleservice.entity.ServiceStockRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceStockRequirementRepository extends JpaRepository<ServiceStockRequirement, Long> {

    List<ServiceStockRequirement> findByService_Id(Long serviceId);
}
