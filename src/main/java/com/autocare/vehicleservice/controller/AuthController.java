package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.entity.User;
import com.autocare.vehicleservice.form.BranchRegisterForm;
import com.autocare.vehicleservice.form.RegisterForm;
import com.autocare.vehicleservice.service.BranchService;
import com.autocare.vehicleservice.service.UserService;
import com.autocare.vehicleservice.util.RoleRedirectUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;
    private final BranchService branchService;

    public AuthController(UserService userService, BranchService branchService) {
        this.userService = userService;
        this.branchService = branchService;
    }


    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        Optional <User> userOptional = userService.authenticate(email, password);

        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/login";
        }

        User user = userOptional.get();

        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("userName", user.getName());
        session.setAttribute("branchId", user.getBranch() != null ? user.getBranch().getId() : null);

        return "redirect:" + RoleRedirectUtil.dashboardPathFor(user.getRole());
    }

    @GetMapping("/register")
    public String customerRegisterPage(Model model){
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }


    @PostMapping("/register")
    public String customerRegisterForm(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                                       BindingResult bindingResult,
                                       Model model,
                                       RedirectAttributes redirectAttributes){

        if (bindingResult.hasErrors()){
            return "register";
        }

        try {
            userService.registerCustomer(registerForm);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("messege", "Registration Successful! Please log in.");
        return "redirect:/login";
    }


    @GetMapping("/register-branch")
    public String branchRegisterPage(Model model){
        model.addAttribute("registerForm", new BranchRegisterForm());
        return "regiter-branch";
    }

    @PostMapping("register-branch")
    public String branchRegisterForm(@Valid @ModelAttribute("registerForm") BranchRegisterForm registerForm,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()){
            return "register-branch";
        }

        try {
            branchService.registerBranch(registerForm);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("message",
                "Branch registration submitted. It's pending approval from the Main Admin - "
                        + "you can log in now, but branch operations open up once it's approved.");

        return "redirect:/login";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();

        return "redirect:/";
    }
}
