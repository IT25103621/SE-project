package com.autocare.vehicleservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

// Fully-qualified on purpose - see the note at the top of entity/Service.java
public interface ServiceRepository extends JpaRepository<com.autocare.vehicleservice.entity.Service, Long> {
}
