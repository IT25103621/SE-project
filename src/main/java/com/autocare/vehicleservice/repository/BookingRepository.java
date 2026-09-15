package com.autocare.vehicleservice.repository;

import com.autocare.vehicleservice.entity.Booking;
import com.autocare.vehicleservice.enums.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByVehicle_IdOrderByCreatedAtDesc(Long vehicleId);

    List<Booking> findByBranch_IdAndStatusOrderByCreatedAtAsc(Long branchId, BookingStatus status);

    List<Booking> findByMechanic_IdAndStatusOrderByCreatedAtAsc(Long mechanicId, BookingStatus status);

    List<Booking> findByMechanic_IdAndStatusOrderByCompletedAtDesc(Long mechanicId, BookingStatus status, Pageable pageable);

    boolean existsByVehicle_Id(Long vehicleId);
}
