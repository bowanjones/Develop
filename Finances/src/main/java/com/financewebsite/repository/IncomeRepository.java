package com.financewebsite.repository;

import com.financewebsite.model.Expense;
import com.financewebsite.model.Income;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    // Additional query methods (if needed) can go here
}