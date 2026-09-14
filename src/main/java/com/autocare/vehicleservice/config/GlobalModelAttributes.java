package com.autocare.vehicleservice.config;

import com.autocare.vehicleservice.enums.Role;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addSessionAttributes(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        if (userId == null) {
            return;
        }
        Role role = (Role) session.getAttribute("userRole");

        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUserName", session.getAttribute("userName"));
        model.addAttribute("isCustomer", role == Role.CUSTOMER);
        model.addAttribute("isCashier", role == Role.CASHIER);
        model.addAttribute("isMechanic", role == Role.MECHANIC);
        model.addAttribute("isInventoryManager", role == Role.INVENTORY_SERVICE_MANAGER);
        model.addAttribute("isBranchManager", role == Role.BRANCH_MANAGER);
        model.addAttribute("isMainAdmin", role == Role.MAIN_ADMIN);
    }
}
