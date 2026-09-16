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

    //Edit method
    @GetMapping("/staff/edit/{id}")
    public String editStaffPage(@PathVariable("id") Long userId,
                                HttpSession session,
                                Model model) {

        Long branchId = (Long) session.getAttribute("branchId");

        Branch branch = branchService.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found."));

        var user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found."));

        // Make sure the staff member belongs to this branch
        if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("Staff member does not belong to this branch.");
        }

        StaffForm form = new StaffForm();
        form.setName(user.getName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRole(user.getRole());

        model.addAttribute("staffForm", form);
        model.addAttribute("staffId", userId);
        model.addAttribute("branch", branch);

        return "branch-manager/staff-edit";
    }

    //UPDATE Method
    @PostMapping("/staff/edit/{id}")
    public String updateStaff(@PathVariable("id") Long userId,
                              @Valid @ModelAttribute("staffForm") StaffForm staffForm,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Long branchId = (Long) session.getAttribute("branchId");

        if (bindingResult.hasErrors()) {
            model.addAttribute("staffId", userId);
            model.addAttribute("branch",
                    branchService.findById(branchId).orElse(null));
            return "branch-manager/staff-edit";
        }

        try {
            Branch branch = branchService.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found."));

            userService.updateStaff(userId, staffForm, branch);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Staff member updated successfully."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/branch-manager/staff";
    }

    //DELETE method
    @PostMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable("id") Long userId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Long branchId = (Long) session.getAttribute("branchId");

        try {
            Branch branch = branchService.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found."));

            userService.deleteStaff(userId, branch);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Staff member deleted successfully."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
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
    public String feedbackPage(com.autocare.vehicleservice.controller.HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        model.addAttribute("feedbackList", feedbackService.listByBranch(branchId));
        return "branch-manager/feedback";
    }
}

