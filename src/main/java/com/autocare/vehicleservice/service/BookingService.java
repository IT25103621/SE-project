package com.autocare.vehicleservice.service;

import com.autocare.vehicleservice.entity.Booking;
import com.autocare.vehicleservice.entity.Branch;
import com.autocare.vehicleservice.entity.User;
import com.autocare.vehicleservice.entity.Vehicle;
import com.autocare.vehicleservice.enums.BookingStatus;
import com.autocare.vehicleservice.enums.BranchStatus;
import com.autocare.vehicleservice.enums.Role;
import com.autocare.vehicleservice.repository.BookingRepository;
import com.autocare.vehicleservice.repository.BranchRepository;
import com.autocare.vehicleservice.repository.VehicleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The core Booking / Job Tracking flow. The base implementation only ever
 * moves a booking PENDING -> ASSIGNED -> COMPLETED (the Inventory & Service
 * Manager's "accept and assign" is one combined step, exactly as the spec
 * describes it) - see enums/BookingStatus.java for a note on the two unused
 * states kept in the schema for future extension.
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;
    private final ServiceCatalogService serviceCatalogService;
    private final UserService userService;

    public BookingService(BookingRepository bookingRepository,
                           VehicleRepository vehicleRepository,
                           BranchRepository branchRepository,
                           ServiceCatalogService serviceCatalogService,
                           UserService userService) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.branchRepository = branchRepository;
        this.serviceCatalogService = serviceCatalogService;
        this.userService = userService;
    }

    public Booking createBooking(Long customerId, Long vehicleId, Long branchId, Long serviceId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found."));
        if (!vehicle.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("That vehicle doesn't belong to you.");
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found."));
        if (branch.getStatus() != BranchStatus.APPROVED) {
            throw new IllegalStateException("That branch isn't accepting bookings yet.");
        }

        com.autocare.vehicleservice.entity.Service service = serviceCatalogService.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found."));

        if (!serviceCatalogService.isAvailable(serviceId, branchId)) {
            throw new IllegalStateException("'" + service.getName()
                    + "' is currently unavailable at this branch due to low stock. Please choose a different service or branch.");
        }

        Booking booking = new Booking();
        booking.setCustomer(vehicle.getCustomer());
        booking.setVehicle(vehicle);
        booking.setBranch(branch);
        booking.setService(service);
        booking.setStatus(BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }

    public List<Booking> listByCustomer(Long customerId) {
        return bookingRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId);
    }

    public List<Booking> listByVehicle(Long vehicleId) {
        return bookingRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId);
    }

    public List<Booking> listPendingByBranch(Long branchId) {
        return bookingRepository.findByBranch_IdAndStatusOrderByCreatedAtAsc(branchId, BookingStatus.PENDING);
    }

    public List<Booking> listAssignedToMechanic(Long mechanicId) {
        return bookingRepository.findByMechanic_IdAndStatusOrderByCreatedAtAsc(mechanicId, BookingStatus.ASSIGNED);
    }

    public List<Booking> listRecentlyCompletedByMechanic(Long mechanicId) {
        Pageable recentTen = PageRequest.of(0, 10);
        return bookingRepository.findByMechanic_IdAndStatusOrderByCompletedAtDesc(mechanicId, BookingStatus.COMPLETED, recentTen);
    }

    public void assignToMechanic(Long bookingId, Long mechanicId, Long branchId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));
        if (!booking.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("That booking doesn't belong to your branch.");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("That booking has already been actioned.");
        }

        User mechanic = userService.findById(mechanicId)
                .orElseThrow(() -> new IllegalArgumentException("Mechanic not found."));
        if (mechanic.getRole() != Role.MECHANIC || mechanic.getBranch() == null
                || !mechanic.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("Choose a mechanic from your own branch.");
        }

        booking.setMechanic(mechanic);
        booking.setStatus(BookingStatus.ASSIGNED);
        bookingRepository.save(booking);
    }

    public void markCompleted(Long bookingId, Long mechanicId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));
        if (booking.getMechanic() == null || !booking.getMechanic().getId().equals(mechanicId)) {
            throw new IllegalStateException("That job isn't assigned to you.");
        }
        if (booking.getStatus() != BookingStatus.ASSIGNED && booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("That job can't be completed from its current status.");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }
}
