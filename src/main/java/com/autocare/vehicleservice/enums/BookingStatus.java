package com.autocare.vehicleservice.enums;

/**
 * Booking lifecycle. The base flow implemented in this skeleton only ever
 * moves PENDING -> ASSIGNED -> COMPLETED (the Inventory & Service Manager
 * accepts and assigns in one step, per the spec). ACCEPTED and IN_PROGRESS
 * are kept here as states because the schema calls for them, and they're a
 * natural extension point if a teammate wants more granular tracking later
 * (e.g. a mechanic explicitly starting a job before completing it).
 */
public enum BookingStatus {
    PENDING,
    ACCEPTED,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED
}
