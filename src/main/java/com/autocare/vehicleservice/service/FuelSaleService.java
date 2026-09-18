package com.autocare.vehicleservice.service;

import com.autocare.vehicleservice.entity.Branch;
import com.autocare.vehicleservice.entity.FuelSale;
import com.autocare.vehicleservice.entity.Receipt;
import com.autocare.vehicleservice.entity.StockItem;
import com.autocare.vehicleservice.entity.User;
import com.autocare.vehicleservice.enums.StockType;
import com.autocare.vehicleservice.repository.FuelSaleRepository;
import com.autocare.vehicleservice.repository.ReceiptRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class FuelSaleService {

    private final FuelSaleRepository fuelSaleRepository;
    private final ReceiptRepository receiptRepository;
    private final StockItemService stockItemService;
    private final com.autocare.vehicleservice.repository.StockItemRepository stockItemRepository;

    public FuelSaleService(FuelSaleRepository fuelSaleRepository,
                            ReceiptRepository receiptRepository,
                            StockItemService stockItemService,
                            com.autocare.vehicleservice.repository.StockItemRepository stockItemRepository) {
        this.fuelSaleRepository = fuelSaleRepository;
        this.receiptRepository = receiptRepository;
        this.stockItemService = stockItemService;
        this.stockItemRepository = stockItemRepository;
    }

    public FuelSale recordSale(Long cashierId, Long branchId, Long stockItemId, BigDecimal quantity, BigDecimal amount) {
        StockItem stockItem = stockItemService.findById(stockItemId)
                .orElseThrow(() -> new IllegalArgumentException("Fuel item not found."));
        if (!stockItem.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("That fuel item doesn't belong to your branch.");
        }
        if (stockItem.getType() != StockType.FUEL) {
            throw new IllegalArgumentException("That stock item isn't a fuel type.");
        }

        stockItemService.deduct(stockItem, quantity);

        User cashierRef = new User();
        cashierRef.setId(cashierId);
        Branch branchRef = new Branch();
        branchRef.setId(branchId);

        FuelSale sale = new FuelSale();
        sale.setCashier(cashierRef);
        sale.setBranch(branchRef);
        sale.setFuelType(stockItem.getName());
        sale.setQuantity(quantity);
        sale.setAmount(amount);
        sale = fuelSaleRepository.save(sale);

        Receipt receipt = new Receipt();
        receipt.setFuelSale(sale);
        receipt.setAmount(amount);
        receiptRepository.save(receipt);

        return sale;
    }

    public List<FuelSale> listRecentByBranch(Long branchId) {
        Pageable recentTen = PageRequest.of(0, 10);
        return fuelSaleRepository.findByBranch_IdOrderBySaleTimeDesc(branchId, recentTen);
    }

    public void deleteSale(Long saleId, Long branchId) {
        FuelSale sale = fuelSaleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Fuel sale not found."));
        if (!sale.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("That fuel sale doesn't belong to your branch.");
        }

        stockItemRepository.findByBranch_IdAndName(branchId, sale.getFuelType())
                .ifPresent(item -> {
                    item.setQuantity(item.getQuantity().add(sale.getQuantity()));
                    stockItemRepository.save(item);
                });

        receiptRepository.deleteByFuelSale_Id(saleId);

        fuelSaleRepository.delete(sale);
    }

    public void updateSale(Long saleId, Long branchId, Long stockItemId, BigDecimal newQuantity, BigDecimal newAmount) {
        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        FuelSale sale = fuelSaleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Fuel sale not found."));
        if (!sale.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("That fuel sale doesn't belong to your branch.");
        }

        StockItem selectedStockItem = null;
        if (stockItemId != null) {
            selectedStockItem = stockItemService.findById(stockItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Fuel item not found."));
            if (!selectedStockItem.getBranch().getId().equals(branchId)) {
                throw new IllegalArgumentException("That fuel item doesn't belong to your branch.");
            }
        }

        if (selectedStockItem != null && !selectedStockItem.getName().equalsIgnoreCase(sale.getFuelType())) {

            stockItemRepository.findByBranch_IdAndName(branchId, sale.getFuelType())
                    .ifPresent(oldItem -> {
                        oldItem.setQuantity(oldItem.getQuantity().add(sale.getQuantity()));
                        stockItemRepository.save(oldItem);
                    });

            if (selectedStockItem.getQuantity().compareTo(newQuantity) < 0) {
                throw new IllegalStateException("Not enough " + selectedStockItem.getName() + " in stock (only "
                        + selectedStockItem.getQuantity() + " " + selectedStockItem.getUnit() + " available).");
            }
            selectedStockItem.setQuantity(selectedStockItem.getQuantity().subtract(newQuantity));
            stockItemRepository.save(selectedStockItem);
            sale.setFuelType(selectedStockItem.getName());
        } else {

            StockItem currentItem = selectedStockItem;
            if (currentItem == null) {
                currentItem = stockItemRepository.findByBranch_IdAndName(branchId, sale.getFuelType())
                        .orElse(null);
            }
            if (currentItem != null) {
                BigDecimal diff = newQuantity.subtract(sale.getQuantity());
                if (diff.compareTo(BigDecimal.ZERO) > 0) {

                    if (currentItem.getQuantity().compareTo(diff) < 0) {
                        throw new IllegalStateException("Not enough " + currentItem.getName() + " in stock (only "
                                + currentItem.getQuantity() + " " + currentItem.getUnit() + " available).");
                    }
                    currentItem.setQuantity(currentItem.getQuantity().subtract(diff));
                } else if (diff.compareTo(BigDecimal.ZERO) < 0) {

                    currentItem.setQuantity(currentItem.getQuantity().add(diff.abs()));
                }
                stockItemRepository.save(currentItem);
            }
        }

        sale.setQuantity(newQuantity);
        sale.setAmount(newAmount);
        fuelSaleRepository.save(sale);

        List<Receipt> receipts = receiptRepository.findByFuelSale_Id(saleId);
        for (Receipt r : receipts) {
            r.setAmount(newAmount);
            receiptRepository.save(r);
        }
    }
}
