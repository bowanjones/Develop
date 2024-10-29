package com.financewebsite.controller;

import com.financewebsite.service.BudgetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("incomes", budgetService.getAllIncomes()); // Assuming you have this method
        model.addAttribute("expenses", budgetService.getAllExpenses()); // Assuming you have this method
        return "index"; // Thymeleaf template name
    }


    @PostMapping("/addIncome")
    public String addIncome(@RequestParam double amount, @RequestParam String category) {
        budgetService.addIncome(amount, category); // Call the service to add income
        return "redirect:/"; // Redirect to home page
    }

    @PostMapping("/addExpense")
    public String addExpense(@RequestParam double amount, @RequestParam String category) {
        budgetService.addExpense(amount, category); // Call the service to add expense
        return "redirect:/"; // Redirect to home page
    }

}
