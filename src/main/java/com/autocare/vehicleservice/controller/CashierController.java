package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.form.SaleForm;
import com.autocare.vehicleservice.service.FuelSaleService;
import com.autocare.vehicleservice.service.StockItemService;
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
@RequestMapping("/cashier")
public class CashierController {

    private final FuelSaleService fuelSaleService;
    private final StockItemService stockItemService;

    public CashierController(FuelSaleService fuelSaleService, StockItemService stockItemService) {
        this.fuelSaleService = fuelSaleService;
        this.stockItemService = stockItemService;
    }

    @GetMapping("/sales")
    public String salesPage(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        model.addAttribute("fuelItems", stockItemService.findFuelItemsByBranch(branchId));
        model.addAttribute("recentSales", fuelSaleService.listRecentByBranch(branchId));
        model.addAttribute("saleForm", new SaleForm());
        return "cashier/sales";
    }

    @PostMapping("/sales")
    public String recordSale(@Valid @ModelAttribute("saleForm") SaleForm saleForm,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        Long cashierId = (Long) session.getAttribute("userId");

        if (saleForm.getStockItemId() != null && saleForm.getQuantity() != null) {
            stockItemService.findById(saleForm.getStockItemId()).ifPresent(item -> {
                if (saleForm.getQuantity().compareTo(item.getQuantity()) > 0) {
                    bindingResult.rejectValue("quantity", "error.quantity",
                            "Stock availability check failed: Entered quantity (" + saleForm.getQuantity() + " L) exceeds tank current stock (" + item.getQuantity() + " " + item.getUnit() + ").");
                }
            });
        }

        if (saleForm.getAmount() != null && saleForm.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            bindingResult.rejectValue("amount", "error.amount", "Amount must be greater than Rs. 0 (> 0).");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("fuelItems", stockItemService.findFuelItemsByBranch(branchId));
            model.addAttribute("recentSales", fuelSaleService.listRecentByBranch(branchId));
            return "cashier/sales";
        }

        try {
            fuelSaleService.recordSale(cashierId, branchId, saleForm.getStockItemId(), saleForm.getQuantity(), saleForm.getAmount());
            redirectAttributes.addFlashAttribute("message", "Sale recorded and receipt generated.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cashier/sales";
    }

    @GetMapping("/update-price")
    public String updatePricePage(HttpSession session, Model model) {
        Long branchId = (Long) session.getAttribute("branchId");
        model.addAttribute("fuelItems", stockItemService.findFuelItemsByBranch(branchId));

        model.addAttribute("history", ((com.autocare.vehicleservice.repository.PriceUpdateHistoryRepository)
                org.springframework.web.context.support.WebApplicationContextUtils
                .getRequiredWebApplicationContext(session.getServletContext())
                .getBean(com.autocare.vehicleservice.repository.PriceUpdateHistoryRepository.class))
                .findByStockItem_Branch_IdOrderByUpdateTimeDesc(branchId));
        return "cashier/update-price";
    }

    @PostMapping("/update-price/{id}")
    public String updatePriceOnly(@org.springframework.web.bind.annotation.PathVariable Long id,
                                  @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal unitPrice,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        String userName = (String) session.getAttribute("userName");
        try {

            com.autocare.vehicleservice.entity.StockItem item = stockItemService.findById(id).orElseThrow();
            stockItemService.updateQuantityAndPrice(id, branchId, item.getQuantity(), unitPrice, userName);
            redirectAttributes.addFlashAttribute("message", "Price updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cashier/update-price";
    }

    @PostMapping("/sales/{id}/delete")
    public String deleteSale(@org.springframework.web.bind.annotation.PathVariable Long id,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        try {
            fuelSaleService.deleteSale(id, branchId);
            redirectAttributes.addFlashAttribute("message", "Fuel sale deleted and stock quantity restored.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cashier/sales";
    }

    @PostMapping("/sales/{id}/update")
    public String updateSale(@org.springframework.web.bind.annotation.PathVariable Long id,
                             @org.springframework.web.bind.annotation.RequestParam(required = false) Long stockItemId,
                             @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal quantity,
                             @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal amount,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        Long branchId = (Long) session.getAttribute("branchId");
        try {
            fuelSaleService.updateSale(id, branchId, stockItemId, quantity, amount);
            redirectAttributes.addFlashAttribute("message", "Fuel sale updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cashier/sales";
    }
}
