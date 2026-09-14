package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.enums.Role;
import com.autocare.vehicleservice.util.RoleRedirectUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId != null) {
            Role role = (Role) session.getAttribute("userRole");
            return "redirect:" + RoleRedirectUtil.dashboardPathFor(role);
        }
        return "index";
    }
}
