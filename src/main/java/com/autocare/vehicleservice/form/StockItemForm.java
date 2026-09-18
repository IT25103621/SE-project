package com.autocare.vehicleservice.form;

import com.autocare.vehicleservice.enums.StockType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class StockItemForm {

    @NotBlank(message = "Please enter an item name")
    @Size(max = 100, message = "Name can be at most 100 characters")
    private String name;

    @NotNull(message = "Please choose a type")
    private StockType type;

    @NotNull(message = "Please enter the starting quantity")
    @DecimalMin(value = "0.00", message = "Quantity can't be negative")
    @Digits(integer = 8, fraction = 2, message = "Use at most 8 digits and 2 decimals")
    private BigDecimal quantity;

    @NotBlank(message = "Please enter a unit (e.g. L, pcs)")
    @Size(max = 20, message = "Unit can be at most 20 characters")
    private String unit;

    @NotNull(message = "Please enter a low-stock threshold")
    @DecimalMin(value = "0.00", message = "Threshold can't be negative")
    @Digits(integer = 8, fraction = 2, message = "Use at most 8 digits and 2 decimals")
    private BigDecimal lowStockThreshold;

    @NotNull(message = "Please enter a unit price")
    @DecimalMin(value = "0.00", message = "Unit price can't be negative")
    @Digits(integer = 8, fraction = 2, message = "Use at most 8 digits and 2 decimals")
    private BigDecimal unitPrice;

    public StockItemForm() { }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StockType getType() { return type; }
    public void setType(StockType type) { this.type = type; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(BigDecimal lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
