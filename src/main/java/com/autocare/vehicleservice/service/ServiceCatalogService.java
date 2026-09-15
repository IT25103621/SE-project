package com.autocare.vehicleservice.service;

import com.autocare.vehicleservice.entity.ServiceStockRequirement;
import com.autocare.vehicleservice.entity.StockItem;
import com.autocare.vehicleservice.repository.ServiceRepository;
import com.autocare.vehicleservice.repository.ServiceStockRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Read-side access to the Service catalog (Oil Change, Tire Rotation, etc.)
 * and the low-stock availability check. The base skeleton doesn't include a
 * UI for managing the catalog itself - no function in the spec calls for
 * one - so services are seeded directly via schema.sql. Adding a management
 * screen for Main Admin or a Branch Manager would be a natural next module.
 *
 * The entity type is fully-qualified throughout this class because its
 * simple name, Service, collides with the org.springframework.stereotype.Service
 * annotation this class itself carries - see entity/Service.java for details.
 */
@Service
@Transactional(readOnly = true)
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final ServiceStockRequirementRepository requirementRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository,
                                  ServiceStockRequirementRepository requirementRepository) {
        this.serviceRepository = serviceRepository;
        this.requirementRepository = requirementRepository;
    }

    public List<com.autocare.vehicleservice.entity.Service> listAll() {
        return serviceRepository.findAll();
    }

    public Optional<com.autocare.vehicleservice.entity.Service> findById(Long id) {
        return serviceRepository.findById(id);
    }

    /**
     * A service is available at a branch unless it has at least one stock
     * requirement whose stock item belongs to that branch AND is currently
     * below its low-stock threshold. A service with no requirement recorded
     * for this particular branch is treated as available, since nothing is
     * known to block it there.
     */
    public boolean isAvailable(Long serviceId, Long branchId) {
        List<ServiceStockRequirement> requirements = requirementRepository.findByService_Id(serviceId);
        for (ServiceStockRequirement requirement : requirements) {
            try {
                StockItem stockItem = requirement.getStockItem();
                if (stockItem == null || stockItem.getBranch() == null || !stockItem.getBranch().getId().equals(branchId)) {
                    continue;
                }
                if (stockItem.isLowStock()) {
                    return false;
                }
            } catch (Exception e) {
                // Ignore missing or orphaned stock items safely
                continue;
            }
        }
        return true;
    }
}
