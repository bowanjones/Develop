package com.financewebsite.service;

import com.financewebsite.model.Expense;
import com.financewebsite.model.Income;
import com.financewebsite.repository.ExpenseRepository;
import com.financewebsite.repository.IncomeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BudgetService {

    private final IncomeRepository incomeRepository; // Assuming you have a repository for incomes
    private final ExpenseRepository expenseRepository; // Assuming you have a repository for expenses

    public BudgetService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    public void addIncome(double amount, String category) {
        Income income = new Income(amount, category); // Assuming you have an Income class
        incomeRepository.save(income);
    }

    public void addExpense(double amount, String category) {
        Expense expense = new Expense(amount, category); // Assuming you have an Expense class
        expenseRepository.save(expense);
    }

    public List<Income> getAllIncomes() {
        return incomeRepository.findAll(); // Fetch all incomes
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll(); // Fetch all expenses
    }
}
