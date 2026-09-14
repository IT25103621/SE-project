package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.service.BranchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BranchService branchService;

    public AdminController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/branches")
    public String branchesPage(Model model) {
        model.addAttribute("pendingBranches", branchService.listPendingBranches());
        model.addAttribute("allBranches", branchService.listAll());
        return "admin/branches";
    }

}
