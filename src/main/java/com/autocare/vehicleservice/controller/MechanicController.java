package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mechanic")
public class MechanicController {

    private final BookingService bookingService;

    public MechanicController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/jobs")
    public String jobsPage(HttpSession session, Model model) {
        Long mechanicId = (Long) session.getAttribute("userId");
        model.addAttribute("assignedJobs", bookingService.listAssignedToMechanic(mechanicId));
        model.addAttribute("completedJobs", bookingService.listRecentlyCompletedByMechanic(mechanicId));
        return "mechanic/jobs";
    }

    @PostMapping("/jobs/{id}/complete")
    public String completeJob(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long mechanicId = (Long) session.getAttribute("userId");
        try {
            bookingService.markCompleted(id, mechanicId);
            redirectAttributes.addFlashAttribute("message", "Job marked as completed.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mechanic/jobs";
    }
}
