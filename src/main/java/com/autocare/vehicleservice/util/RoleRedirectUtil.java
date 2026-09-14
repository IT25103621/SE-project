package com.autocare.vehicleservice.util;


import com.autocare.vehicleservice.enums.Role;

public final class RoleRedirectUtil {

    private RoleRedirectUtil() {
    }

    public static String dashboardPathFor(Role role) {
        if (role == null) {
            return "/login";
        }
        switch (role) {
            case CUSTOMER:
                return "/customer/vehicles";
            case CASHIER:
                return "/cashier/sales";
            case MECHANIC:
                return "/mechanic/jobs";
            case INVENTORY_SERVICE_MANAGER:
                return "/inventory/bookings";
            case BRANCH_MANAGER:
                return "/branch-manager/staff";
            case MAIN_ADMIN:
                return "/admin/branches";
            default:
                return "/login";
        }
    }
}

