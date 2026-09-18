package com.autocare.vehicleservice.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class SaleForm {

    @NotNull(message = "Please select a fuel type")
    private Long stockItemId;

    @NotNull(message = "Quantity (Litres) is required and cannot be empty.")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0 (> 0). Negative numbers and zero are not allowed.")
    private BigDecimal quantity;

    @NotNull(message = "Amount (Rs.) is required.")
    @DecimalMin(value = "0.01", message = "Amount must be greater than Rs. 0 (> 0).")
    private BigDecimal amount;

    public Long getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Long stockItemId) {
        this.stockItemId = stockItemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
