package com.autocare.vehicleservice.service;

import com.autocare.vehicleservice.entity.Branch;
import com.autocare.vehicleservice.entity.StockItem;
import com.autocare.vehicleservice.enums.StockType;
import com.autocare.vehicleservice.form.StockItemForm;
import com.autocare.vehicleservice.repository.StockItemRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StockItemService {

    private final StockItemRepository stockItemRepository;

    public StockItemService(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    public List<StockItem> listByBranch(Long branchId) {
        return stockItemRepository.findByBranch_IdOrderByNameAsc(branchId);
    }

    public List<StockItem> findFuelItemsByBranch(Long branchId) {
        return stockItemRepository.findByBranch_IdAndTypeOrderByNameAsc(branchId, StockType.FUEL);
    }

    public Optional<StockItem> findById(Long id) {
        return stockItemRepository.findById(id);
    }

    public StockItem create(StockItemForm form, Long branchId) {
        String name = form.getName().trim();
        if (stockItemRepository.findByBranch_IdAndName(branchId, name).isPresent()) {
            throw new IllegalArgumentException("\"" + name + "\" is already in your stock list. Update its quantity instead.");
        }

        Branch branchRef = new Branch();
        branchRef.setId(branchId);

        StockItem item = new StockItem();
        item.setBranch(branchRef);
        item.setName(name);
        item.setType(form.getType());
        item.setQuantity(form.getQuantity());
        item.setUnit(form.getUnit().trim());
        item.setLowStockThreshold(form.getLowStockThreshold());
        item.setUnitPrice(form.getUnitPrice());
        return stockItemRepository.save(item);
    }

    public void updateQuantity(Long stockItemId, Long branchId, BigDecimal newQuantity) {
        StockItem item = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new IllegalArgumentException("Stock item not found."));
        if (!item.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("That stock item doesn't belong to your branch.");
        }
        item.setQuantity(newQuantity);
        stockItemRepository.save(item);
    }

    public StockItem update(Long stockItemId, Long branchId, String name, StockType type,
                            BigDecimal quantity, String unit, BigDecimal lowStockThreshold,
                            BigDecimal unitPrice) {
        StockItem item = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new IllegalArgumentException("Stock item not found."));
        if (!item.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("That stock item doesn't belong to your branch.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter an item name.");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 100) {
            throw new IllegalArgumentException("Name can be at most 100 characters.");
        }
        stockItemRepository.findByBranch_IdAndName(branchId, trimmedName)
                .filter(other -> !other.getId().equals(stockItemId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("\"" + trimmedName + "\" is already used by another stock item.");
                });

        if (type == null) {
            throw new IllegalArgumentException("Please choose a type.");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity can't be negative.");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a unit (e.g. L, pcs).");
        }
        if (lowStockThreshold == null || lowStockThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Threshold can't be negative.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price can't be negative.");
        }

        item.setName(trimmedName);
        item.setType(type);
        item.setQuantity(quantity);
        item.setUnit(unit.trim());
        item.setLowStockThreshold(lowStockThreshold);
        item.setUnitPrice(unitPrice);
        return stockItemRepository.save(item);
    }

    public void delete(Long stockItemId, Long branchId) {
        StockItem item = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new IllegalArgumentException("Stock item not found."));
        if (!item.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("That stock item doesn't belong to your branch.");
        }
        try {
            stockItemRepository.delete(item);
            stockItemRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("\"" + item.getName()
                    + "\" can't be deleted because it's already linked to a service or past sale. "
                    + "Set its quantity to 0 instead if it's no longer used.");
        }
    }

    public void deduct(StockItem item, BigDecimal amount) {
        if (item.getQuantity().compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough " + item.getName() + " in stock (only "
                    + item.getQuantity() + " " + item.getUnit() + " available).");
        }
        item.setQuantity(item.getQuantity().subtract(amount));
        stockItemRepository.save(item);
    }
}