package com.autocare.vehicleservice.interceptor;

import com.autocare.vehicleservice.enums.Role;
import com.autocare.vehicleservice.util.RoleRedirectUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Object userId = session != null ? session.getAttribute("userId") : null;

        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String path = request.getRequestURI();
        Role role = (Role) session.getAttribute("userRole");
        Role requiredRole = requiredRoleFor(path);

        if (requiredRole != null && role != requiredRole) {
            response.sendRedirect(request.getContextPath() + RoleRedirectUtil.dashboardPathFor(role));
            return false;
        }

        return true;
    }

    private Role requiredRoleFor(String path) {
        if (path.startsWith("/customer")) {
            return Role.CUSTOMER;
        }
        if (path.startsWith("/cashier")) {
            return Role.CASHIER;
        }
        if (path.startsWith("/mechanic")) {
            return Role.MECHANIC;
        }
        if (path.startsWith("/inventory")) {
            return Role.INVENTORY_SERVICE_MANAGER;
        }
        if (path.startsWith("/branch-manager")) {
            return Role.BRANCH_MANAGER;
        }
        if (path.startsWith("/admin")) {
            return Role.MAIN_ADMIN;
        }
        return null;
    }
}
