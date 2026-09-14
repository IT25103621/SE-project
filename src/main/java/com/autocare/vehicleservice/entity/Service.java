package com.autocare.vehicleservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * The catalog of service offerings (Oil Change, Tire Rotation, etc.).
 *
 * NOTE: naming this class "Service" (per the spec's exact entity name) means
 * it shares a simple name with org.springframework.stereotype.Service, the
 * annotation used on every business-logic class in this project. That's not
 * a problem here (this file doesn't need the annotation), but it does mean
 * ServiceCatalogService.java and BookingService.java - the two @Service
 * classes that also need this entity type - use the fully-qualified name
 * com.autocare.vehicleservice.entity.Service internally to keep the two apart.
 */
@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public Service() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
