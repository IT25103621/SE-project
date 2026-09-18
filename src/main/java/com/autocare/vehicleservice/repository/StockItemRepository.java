package com.autocare.vehicleservice.repository;

import com.autocare.vehicleservice.entity.StockItem;
import com.autocare.vehicleservice.enums.StockType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    List<StockItem> findByBranch_IdOrderByNameAsc(Long branchId);

    List<StockItem> findByBranch_IdAndTypeOrderByNameAsc(Long branchId, StockType type);

    Optional<StockItem> findByBranch_IdAndName(Long branchId, String name);
}
