package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.enums.StockType;
import com.autocare.vehicleservice.form.StockItemForm;
import com.autocare.vehicleservice.service.BookingService;
import com.autocare.vehicleservice.service.StockItemService;
import com.autocare.vehicleservice.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final BookingService bookingService;
    private final UserService userService;
    private final StockItemService stockItemService;

    public InventoryController(BookingService bookingService, UserService userService, StockItemService stockItemService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.stockItemService = stockItemService;
    }

    // Service Booking & Job Tracking


    @GetMapping("/bookings")
    public String pendingBookings(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        model.addAttribute("bookings", bookingService.listPendingByBranch(branchId));
        model.addAttribute("activeJobs", bookingService.listActiveByBranch(branchId));
        model.addAttribute("completedJobs", bookingService.listRecentlyCompletedByBranch(branchId));
        model.addAttribute("mechanics", userService.findMechanicsByBranch(branchId));
        return "inventory/bookings";
    }

    @PostMapping("/bookings/{id}/assign")
    public String assign(@PathVariable Long id, @RequestParam Long mechanicId,
                         HttpSession session, RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        try {
            bookingService.assignToMechanic(id, mechanicId, branchId);
            redirectAttributes.addFlashAttribute("message", "Booking assigned to mechanic.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Could not assign booking.");
        }
        return "redirect:/inventory/bookings";
    }

    @PostMapping("/bookings/{id}/reject")
    public String reject(@PathVariable Long id,
                         HttpSession session, RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        try {
            bookingService.rejectBooking(id, branchId);
            redirectAttributes.addFlashAttribute("message", "Booking rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Could not reject booking.");
        }
        return "redirect:/inventory/bookings";
    }


    // Inventory Management


    @GetMapping("/stock")
    public String stockPage(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        populateStockPage(model, branchId);
        model.addAttribute("stockItemForm", new StockItemForm());
        return "inventory/stock";
    }

    @PostMapping("/stock")
    public String addStockItem(@Valid @ModelAttribute("stockItemForm") StockItemForm stockItemForm,
                               BindingResult bindingResult,
                               HttpSession session, Model model,
                               RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        if (!bindingResult.hasErrors()) {
            try {
                stockItemService.create(stockItemForm, branchId);
                redirectAttributes.addFlashAttribute("message", "Stock item added.");
                return "redirect:/inventory/stock";
            } catch (IllegalArgumentException e) {
                // e.g. duplicate name - show it next to the field and keep what the user typed
                bindingResult.rejectValue("name", "duplicate", e.getMessage());
            }
        }
        populateStockPage(model, branchId);
        return "inventory/stock";
    }

    @PostMapping("/stock/{id}/edit")
    public String editStockItem(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam StockType type,
                                @RequestParam BigDecimal quantity,
                                @RequestParam String unit,
                                @RequestParam BigDecimal lowStockThreshold,
                                @RequestParam BigDecimal unitPrice,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        try {
            stockItemService.update(id, branchId, name, type, quantity, unit, lowStockThreshold, unitPrice);
            redirectAttributes.addFlashAttribute("message", "Stock item updated.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory/stock";
    }

    @PostMapping("/stock/{id}/delete")
    public String deleteStockItem(@PathVariable Long id,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        try {
            stockItemService.delete(id, branchId);
            redirectAttributes.addFlashAttribute("message", "Stock item deleted.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory/stock";
    }

    private void populateStockPage(Model model, Long branchId) {
        model.addAttribute("stockItems", stockItemService.listByBranch(branchId));
        model.addAttribute("stockTypes", StockType.values());
    }
}
