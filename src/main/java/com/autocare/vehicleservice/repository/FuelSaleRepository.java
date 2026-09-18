package com.autocare.vehicleservice.repository;

import com.autocare.vehicleservice.entity.FuelSale;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuelSaleRepository extends JpaRepository<FuelSale, Long> {

    List<FuelSale> findByBranch_IdOrderBySaleTimeDesc(Long branchId, Pageable pageable);
}
