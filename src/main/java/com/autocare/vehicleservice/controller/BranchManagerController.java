package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.entity.Branch;
import com.autocare.vehicleservice.entity.User;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/branch-manager")
public class BranchManagerController {

    private final UserService userService;
    private final BranchService branchService;
    private final FeedbackService feedbackService;

    public BranchManagerController(UserService userService, BranchService branchService,
            FeedbackService feedbackService) {
        this.userService = userService;
        this.branchService = branchService;
        this.feedbackService = feedbackService;
    }

    // Staff list + Add

    @GetMapping("/staff")
    public String staffPage(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        model.addAttribute("staffList", userService.findStaffByBranch(branchId));
        model.addAttribute("branch", branchService.findById(branchId).orElse(null));
        model.addAttribute("staffForm", new StaffForm());
        return "branch-manager/staff";
    }

    // Create/Add Staff method

    @PostMapping("/staff")
    public String addStaff(@Valid @ModelAttribute("staffForm") StaffForm staffForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");

        // Password is required when creating a new staff member (optional only on edit)
        if (staffForm.getPassword() == null || staffForm.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "required", "Please choose a password");
        }

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

    // Staff Edit Method to update

    @GetMapping("/staff/{id}/edit")
    public String editStaffForm(@PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        User staff = userService.findById(id).orElse(null);

        if (staff == null || staff.getBranch() == null || !staff.getBranch().getId().equals(branchId)) {
            redirectAttributes.addFlashAttribute("error", "Staff member not found.");
            return "redirect:/branch-manager/staff";
        }

        // Already fill the form with existing information but leave password blank
        StaffForm form = new StaffForm();
        form.setName(staff.getName());
        form.setEmail(staff.getEmail());
        form.setPhone(staff.getPhone());
        form.setRole(staff.getRole());

        model.addAttribute("staffForm", form);
        model.addAttribute("staffId", id);
        model.addAttribute("branch", branchService.findById(branchId).orElse(null));
        return "branch-manager/staff-edit";
    }

    @PostMapping("/staff/{id}/edit")
    public String editStaff(@PathVariable Long id,
            @Valid @ModelAttribute("staffForm") StaffForm staffForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");

        if (bindingResult.hasErrors()) {
            model.addAttribute("staffId", id);
            model.addAttribute("branch", branchService.findById(branchId).orElse(null));
            return "branch-manager/staff-edit";
        }

        try {
            userService.updateStaff(id, staffForm, branchId);
            redirectAttributes.addFlashAttribute("message", "Staff member updated.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/branch-manager/staff";
    }

    // Staff Delete method to remove a staff membr

    @PostMapping("/staff/{id}/delete")
    public String deleteStaff(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        try {
            userService.deleteStaff(id, branchId);
            redirectAttributes.addFlashAttribute("message", "Staff member removed.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/branch-manager/staff";
    }

    // Branch details page display

    @GetMapping("/branch")
    public String branchDetailsPage(HttpSession session, Model model) {

        Long branchId = (Long) session.getAttribute("branchId");

        if (branchId == null) {
            return "redirect:/login";
        }

        Branch branch = branchService.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found."));

        model.addAttribute("branch", branch);

        return "branch-manager/branch";
    }

    // Feedback Page method

    @GetMapping("/feedback")
    public String feedbackPage(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        model.addAttribute("feedbackList", feedbackService.listByBranch(branchId));
        return "branch-manager/feedback";
    }
}
