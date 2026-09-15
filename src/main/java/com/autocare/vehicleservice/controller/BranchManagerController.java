package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.entity.Branch;
import com.autocare.vehicleservice.form.StaffForm;
import com.autocare.vehicleservice.service.BranchService;
import com.autocare.vehicleservice.service.FeedbackService;
import com.autocare.vehicleservice.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/branch-manager")
public class BranchManagerController {

    private final UserService userService;
    private final BranchService branchService;
    private final FeedbackService feedbackService;

    public BranchManagerController(UserService userService, BranchService branchService, FeedbackService feedbackService) {
        this.userService = userService;
        this.branchService = branchService;
        this.feedbackService = feedbackService;
    }
    @GetMapping("/staff")
    public String staffPage(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        model.addAttribute("staffList", userService.findStaffByBranch(branchId));
        model.addAttribute("branch", branchService.findById(branchId).orElse(null));
        model.addAttribute("staffForm", new StaffForm());
        return "branch-manager/staff";
    }
    @PostMapping("/staff")
    public String addStaff(@Valid @ModelAttribute("staffForm") StaffForm staffForm,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");

        if (bindingResult.hasErrors()) {
            model.addAttribute("staffList", userService.findStaffByBranch(branchId));
            model.addAttribute("branch", branchService.findById(branchId).orElse(null));
            return "branch-manager/staff";
        }

        try {
            Branch branch = branchService.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found."));
            userService.createStaff(staffForm, branch);
            redirectAttributes.addFlashAttribute("message", "Staff member added.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/branch-manager/staff";
    }

    @GetMapping("/branch")
    public String branchDetailsPage(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");

        Branch branch = branchService.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found."));

        model.addAttribute("branch", branch);

        return "branch-manager/branch";
    }

    @GetMapping("/feedback")
    public String feedbackPage(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        model.addAttribute("feedbackList", feedbackService.listByBranch(branchId));
        return "branch-manager/feedback";
    }
}

