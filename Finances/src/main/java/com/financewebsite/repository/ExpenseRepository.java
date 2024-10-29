package com.financewebsite.repository;

import com.financewebsite.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Additional query methods (if needed) can go here
}